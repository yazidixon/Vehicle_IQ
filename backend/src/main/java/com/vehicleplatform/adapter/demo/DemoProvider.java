package com.vehicleplatform.adapter.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.vehicleplatform.adapter.NormalizedVehicleSpec;
import com.vehicleplatform.adapter.VehicleDataProvider;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Local curated dataset used ONLY as a clearly-labeled "Demo data" fallback
 * when no free/keyless full-spec provider is configured (see
 * docs/api-comparison.md). Every vehicle from this provider is tagged
 * demoData=true end to end so it is never confused with a live API response.
 */
@Slf4j
@Component
public class DemoProvider implements VehicleDataProvider {

    public static final String NAME = "demo";
    private static final String DATA_FILE = "data/demo_vehicles.json";

    // Owns a plain Jackson 2 ObjectMapper (see JsonUtil) rather than injecting
    // Spring's autoconfigured bean, which is Jackson 3 in Spring Boot 4.
    private final ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    private List<DemoVehicleRow> rows = List.of();

    @PostConstruct
    void load() {
        try (var input = new ClassPathResource(DATA_FILE).getInputStream()) {
            rows = objectMapper.readValue(input, new com.fasterxml.jackson.core.type.TypeReference<List<DemoVehicleRow>>() {});
            log.info("Loaded {} demo vehicles from {}", rows.size(), DATA_FILE);
        } catch (IOException exc) {
            log.error("Unable to load demo dataset from {}: {}", DATA_FILE, exc.getMessage());
            rows = List.of();
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isConfigured() {
        return true;
    }

    public List<DemoVehicleRow> rows() {
        return rows;
    }

    private NormalizedVehicleSpec toNormalized(DemoVehicleRow r) {
        return NormalizedVehicleSpec.builder()
                .manufacturer(r.manufacturer()).make(r.make()).model(r.model()).generation(r.generation())
                .year(r.year()).trim(r.trim()).bodyType(r.bodyType()).fuelType(r.fuelType())
                .engineCode(r.engineCode()).engineType(r.engineType())
                .engineDisplacementCc(r.engineDisplacementCc()).horsepowerHp(r.horsepowerHp())
                .torqueNm(r.torqueNm()).transmission(r.transmission()).drivetrain(r.drivetrain())
                .topSpeedKmh(r.topSpeedKmh()).acceleration0100S(r.acceleration0100S())
                .lengthMm(r.lengthMm()).widthMm(r.widthMm()).heightMm(r.heightMm()).wheelbaseMm(r.wheelbaseMm())
                .weightKg(r.weightKg()).doors(r.doors()).seats(r.seats())
                .fuelConsumptionUrbanL100km(r.fuelConsumptionUrbanL100km())
                .fuelConsumptionExtraUrbanL100km(r.fuelConsumptionExtraUrbanL100km())
                .fuelConsumptionMixedL100km(r.fuelConsumptionMixedL100km())
                .co2EmissionsGkm(r.co2EmissionsGkm()).euroNorm(r.euroNorm()).energyClass(r.energyClass())
                .priceMsrp(r.priceMsrp()).currency(r.currency())
                .equipment(r.equipment() != null ? r.equipment() : List.of())
                .safetyNotes(r.safetyNotes())
                .sourceProvider(NAME)
                .sourceEndpoint("local://data/demo_vehicles.json")
                .demoData(true)
                .build();
    }

    @Override
    public List<NormalizedVehicleSpec> search(String make, String model, Integer year, String fuelType, String q) {
        List<NormalizedVehicleSpec> results = new ArrayList<>();
        for (DemoVehicleRow r : rows) {
            if (make != null && !r.make().equalsIgnoreCase(make)) continue;
            if (model != null && !r.model().toLowerCase().contains(model.toLowerCase())) continue;
            if (year != null && !year.equals(r.year())) continue;
            if (fuelType != null && (r.fuelType() == null || !r.fuelType().toLowerCase().contains(fuelType.toLowerCase()))) continue;
            if (q != null) {
                String needle = q.toLowerCase();
                boolean matches = r.make().toLowerCase().contains(needle) || r.model().toLowerCase().contains(needle)
                        || r.manufacturer().toLowerCase().contains(needle);
                if (!matches) continue;
            }
            results.add(toNormalized(r));
        }
        return results;
    }

    @Override
    public List<String> listMakes() {
        var set = new TreeSet<String>();
        rows.forEach(r -> set.add(r.make()));
        return new ArrayList<>(set);
    }

    @Override
    public List<String> listModels(String make) {
        var set = new TreeSet<String>();
        rows.forEach(r -> {
            if (r.make().equalsIgnoreCase(make)) set.add(r.model());
        });
        return new ArrayList<>(set);
    }
}
