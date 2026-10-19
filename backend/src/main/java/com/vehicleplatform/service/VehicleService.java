package com.vehicleplatform.service;

import com.vehicleplatform.adapter.NormalizedVehicleSpec;
import com.vehicleplatform.adapter.ProviderException;
import com.vehicleplatform.adapter.apininjas.ApiNinjasProvider;
import com.vehicleplatform.adapter.demo.DemoProvider;
import com.vehicleplatform.adapter.nhtsa.NhtsaProvider;
import com.vehicleplatform.config.AppProperties;
import com.vehicleplatform.dto.vehicle.VehicleSearchFilters;
import com.vehicleplatform.entity.Vehicle;
import com.vehicleplatform.entity.VehicleSourceData;
import com.vehicleplatform.entity.VehicleSpecification;
import com.vehicleplatform.repository.VehicleRepository;
import com.vehicleplatform.repository.VehicleSourceDataRepository;
import com.vehicleplatform.repository.VehicleSpecifications;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleSourceDataRepository sourceDataRepository;
    private final NhtsaProvider nhtsaProvider;
    private final DemoProvider demoProvider;
    private final ApiNinjasProvider apiNinjasProvider;
    private final CacheService cacheService;
    private final AppProperties appProperties;

    public VehicleService(VehicleRepository vehicleRepository, VehicleSourceDataRepository sourceDataRepository,
                           NhtsaProvider nhtsaProvider, DemoProvider demoProvider, ApiNinjasProvider apiNinjasProvider,
                           CacheService cacheService, AppProperties appProperties) {
        this.vehicleRepository = vehicleRepository;
        this.sourceDataRepository = sourceDataRepository;
        this.nhtsaProvider = nhtsaProvider;
        this.demoProvider = demoProvider;
        this.apiNinjasProvider = apiNinjasProvider;
        this.cacheService = cacheService;
        this.appProperties = appProperties;
    }

    private static Specification<Vehicle> and(Specification<Vehicle> base, Specification<Vehicle> addition) {
        if (addition == null) return base;
        return base == null ? addition : base.and(addition);
    }

    /** Curated makes used only to seed the catalog view with real API Ninjas data on first load. */
    private static final List<String> CATALOG_SEED_MAKES = List.of(
            "Toyota", "Honda", "Ford", "Bmw", "Mercedes-Benz", "Audi", "Volkswagen",
            "Chevrolet", "Nissan", "Hyundai", "Kia", "Porsche", "Mazda", "Subaru", "Volvo"
    );
    private static final int CATALOG_SEED_TARGET = 60;

    @Transactional
    public Page<Vehicle> search(VehicleSearchFilters filters) {
        boolean isEmptyBrowse = filters.q() == null && filters.make() == null && filters.model() == null
                && filters.manufacturer() == null && filters.year() == null && filters.yearMin() == null
                && filters.yearMax() == null && filters.fuelType() == null && filters.engineCode() == null;
        if (isEmptyBrowse) {
            // Catalog landing view (no filters yet): make sure it's backed by real
            // API Ninjas data rather than only the demo catalog, without ever
            // re-calling the API once this has run once (see ensureCatalogSeeded).
            ensureCatalogSeeded();
        }

        // Spring Data JPA 4.x's Specification.where(null) now throws instead of
        // acting as a no-op start value, so we combine from a plain null reference.
        Specification<Vehicle> spec = null;
        spec = and(spec, filters.make() != null ? VehicleSpecifications.makeEquals(filters.make()) : null);
        spec = and(spec, filters.model() != null ? VehicleSpecifications.modelContains(filters.model()) : null);
        spec = and(spec, filters.manufacturer() != null ? VehicleSpecifications.manufacturerEquals(filters.manufacturer()) : null);
        spec = and(spec, filters.year() != null ? VehicleSpecifications.yearEquals(filters.year()) : null);
        spec = and(spec, filters.yearMin() != null ? VehicleSpecifications.yearGte(filters.yearMin()) : null);
        spec = and(spec, filters.yearMax() != null ? VehicleSpecifications.yearLte(filters.yearMax()) : null);
        spec = and(spec, filters.fuelType() != null ? VehicleSpecifications.fuelTypeContains(filters.fuelType()) : null);
        spec = and(spec, filters.engineCode() != null ? VehicleSpecifications.engineCodeContains(filters.engineCode()) : null);
        spec = and(spec, filters.q() != null ? VehicleSpecifications.freeText(filters.q()) : null);

        var pageable = PageRequest.of(Math.max(filters.page() - 1, 0), filters.pageSize(),
                Sort.by(Sort.Order.asc("make"), Sort.Order.asc("model"), Sort.Order.desc("year")));

        Page<Vehicle> page = vehicleRepository.findAll(spec, pageable);

        // Local catalog (demo data + anything seeded so far) has nothing for this
        // query: try a live API Ninjas lookup and persist whatever comes back so
        // it joins the searchable catalog permanently (one live call per new
        // make/model/year combination, ever).
        if (page.getTotalElements() == 0 && apiNinjasProvider.isConfigured()
                && (filters.make() != null || filters.model() != null || filters.q() != null)) {
            fetchAndPersistFromApiNinjas(filters);
            page = vehicleRepository.findAll(spec, pageable);
        }

        page.getContent().forEach(v -> {
            if (v.getSpecification() != null) v.getSpecification().getEquipment().size(); // force lazy init
        });
        return page;
    }

    private void fetchAndPersistFromApiNinjas(VehicleSearchFilters filters) {
        try {
            String make = filters.make();
            String model = filters.model();
            // Free-text search ("Porsche Macan") never had make/model split apart,
            // so API Ninjas previously received the whole phrase as "make" and
            // matched nothing. Split it here — once — before calling the provider;
            // searches that already pass make/model explicitly are untouched.
            if (make == null && model == null && filters.q() != null) {
                MakeModelSplit split = splitFreeText(filters.q());
                make = split.make();
                model = split.model();
            }

            List<NormalizedVehicleSpec> liveResults = apiNinjasProvider.search(
                    make, model, filters.year(), filters.fuelType(), filters.q());
            for (NormalizedVehicleSpec spec : liveResults) {
                persistIfNew(spec);
            }
        } catch (ProviderException exc) {
            log.warn("API Ninjas live search failed, returning empty results: {}", exc.getMessage());
        }
    }

    /**
     * One-time catalog bootstrap: pulls a bounded batch of real API Ninjas
     * vehicles (across a handful of popular makes) so the search page has
     * genuine live data to show as soon as it opens, instead of only the demo
     * catalog. Guarded by a long-TTL cache sentinel (see {@link #ensureCatalogSeeded()})
     * so this runs at most once per TTL window, no matter how many times the
     * empty catalog view is loaded.
     */
    private void ensureCatalogSeeded() {
        if (!apiNinjasProvider.isConfigured()) return;
        try {
            cacheService.getOrFetch(
                    ApiNinjasProvider.NAME, "CatalogSeed", Map.of(),
                    () -> Map.of("insertedCount", seedCatalogFromApiNinjas()),
                    24L * 30
            );
        } catch (ProviderException exc) {
            log.warn("Catalog seed from API Ninjas failed: {}", exc.getMessage());
        }
    }

    private int seedCatalogFromApiNinjas() {
        int totalInserted = 0;
        for (String make : CATALOG_SEED_MAKES) {
            if (totalInserted >= CATALOG_SEED_TARGET) break;
            try {
                List<NormalizedVehicleSpec> results = apiNinjasProvider.search(make, null, null, null, null);
                for (NormalizedVehicleSpec spec : results) {
                    if (totalInserted >= CATALOG_SEED_TARGET) break;
                    if (persistIfNew(spec)) totalInserted++;
                }
            } catch (ProviderException exc) {
                log.warn("Catalog seed: API Ninjas call failed for make={}: {}", make, exc.getMessage());
            }
        }
        log.info("Catalog seed complete: {} new vehicle(s) inserted from API Ninjas.", totalInserted);
        return totalInserted;
    }

    /** Persists one API Ninjas result if it's not already in the catalog. Returns true if inserted. */
    private boolean persistIfNew(NormalizedVehicleSpec spec) {
        if (spec.getMake() == null || spec.getModel() == null || spec.getYear() == null) return false;
        boolean exists = vehicleRepository
                .existsByMakeIgnoreCaseAndModelIgnoreCaseAndYear(spec.getMake(), spec.getModel(), spec.getYear());
        if (exists) return false;

        Vehicle vehicle = Vehicle.builder()
                .manufacturer(spec.getManufacturer() != null ? spec.getManufacturer() : spec.getMake())
                .make(spec.getMake()).model(spec.getModel()).year(spec.getYear())
                .bodyType(spec.getBodyType()).fuelType(spec.getFuelType())
                .sourceProvider(ApiNinjasProvider.NAME).demoData(false)
                .build();
        vehicle = vehicleRepository.save(vehicle);

        VehicleSpecification specification = VehicleSpecification.builder()
                .vehicle(vehicle)
                .engineDisplacementCc(spec.getEngineDisplacementCc())
                .transmission(spec.getTransmission())
                .drivetrain(spec.getDrivetrain())
                .demoData(false)
                .build();
        vehicle.setSpecification(specification);
        vehicleRepository.save(vehicle);

        recordApiNinjasSourceData(vehicle, spec);
        return true;
    }

    private record MakeModelSplit(String make, String model) {
    }

    /**
     * Splits a free-text query ("Porsche Macan", "BMW X5") into make + model.
     * Preferred strategy: check whether the query starts with a make the app
     * already knows about (the same cached list used for the "Marque"
     * autocomplete — see {@link #listMakes()}), picking the longest match so
     * multi-word makes like "Land Rover" or "Alfa Romeo" aren't cut short.
     * Falls back to "first word = make, rest = model" only when no known make
     * matches — which also happens to be exactly right for the single-word
     * makes in the examples ("Porsche Macan", "BMW X5", "Toyota Corolla").
     * A query matching a known make with nothing after it (just "Porsche")
     * yields model=null, so a make-only search still behaves exactly as before.
     */
    private MakeModelSplit splitFreeText(String q) {
        String trimmed = q.trim();

        String bestMake = null;
        for (String candidate : listMakes()) {
            if (candidate == null || candidate.isBlank()) continue;
            boolean isExactMatch = trimmed.equalsIgnoreCase(candidate);
            boolean isPrefixMatch = trimmed.length() > candidate.length()
                    && trimmed.substring(0, candidate.length()).equalsIgnoreCase(candidate)
                    && trimmed.charAt(candidate.length()) == ' ';
            if ((isExactMatch || isPrefixMatch) && (bestMake == null || candidate.length() > bestMake.length())) {
                bestMake = candidate;
            }
        }
        if (bestMake != null) {
            String remainder = trimmed.substring(bestMake.length()).trim();
            return new MakeModelSplit(bestMake, remainder.isBlank() ? null : remainder);
        }

        int spaceIndex = trimmed.indexOf(' ');
        if (spaceIndex <= 0) {
            return new MakeModelSplit(trimmed, null);
        }
        return new MakeModelSplit(trimmed.substring(0, spaceIndex), trimmed.substring(spaceIndex + 1).trim());
    }

    private void recordApiNinjasSourceData(Vehicle vehicle, NormalizedVehicleSpec spec) {
        Instant now = Instant.now();
        Map<String, Object> fields = new java.util.LinkedHashMap<>();
        fields.put("engine_displacement_cc", spec.getEngineDisplacementCc());
        fields.put("transmission", spec.getTransmission());
        fields.put("drivetrain", spec.getDrivetrain());
        fields.put("fuel_type", spec.getFuelType());
        fields.put("body_type", spec.getBodyType());
        fields.forEach((fieldName, value) -> {
            if (value == null) return;
            sourceDataRepository.save(VehicleSourceData.builder()
                    .vehicle(vehicle).fieldName(fieldName).fieldValue(String.valueOf(value))
                    .sourceProvider(ApiNinjasProvider.NAME).sourceEndpoint("https://api.api-ninjas.com/v1/cars")
                    .demoData(false).retrievedAt(now)
                    .build());
        });
    }

    @Transactional(readOnly = true)
    public Optional<Vehicle> getById(UUID id) {
        Optional<Vehicle> vehicle = vehicleRepository.findById(id);
        vehicle.ifPresent(v -> {
            if (v.getSpecification() != null) v.getSpecification().getEquipment().size();
        });
        return vehicle;
    }

    public List<com.vehicleplatform.entity.VehicleSourceData> getSources(UUID vehicleId) {
        return sourceDataRepository.findByVehicleId(vehicleId);
    }

    @SuppressWarnings("unchecked")
    public List<String> listMakes() {
        try {
            var outcome = cacheService.getOrFetch(
                    nhtsaProvider.getName(), "GetAllMakes", Map.of(),
                    () -> Map.of("makes", nhtsaProvider.listMakes()),
                    24L * 7
            );
            return (List<String>) outcome.payload().get("makes");
        } catch (ProviderException exc) {
            log.warn("NHTSA makes list unavailable, falling back: {}", exc.getMessage());
            List<String> local = vehicleRepository.findDistinctMakes();
            if (!local.isEmpty()) return local;
            return appProperties.demoMode() ? demoProvider.listMakes() : List.of();
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> listModels(String make) {
        try {
            var outcome = cacheService.getOrFetch(
                    nhtsaProvider.getName(), "GetModelsForMake", Map.of("make", make),
                    () -> Map.of("models", nhtsaProvider.listModels(make)),
                    24L * 7
            );
            return (List<String>) outcome.payload().get("models");
        } catch (ProviderException exc) {
            log.warn("NHTSA models list unavailable, falling back: {}", exc.getMessage());
            List<String> local = vehicleRepository.findDistinctModelsByMake(make);
            if (!local.isEmpty()) return local;
            return appProperties.demoMode() ? demoProvider.listModels(make) : List.of();
        }
    }
}
