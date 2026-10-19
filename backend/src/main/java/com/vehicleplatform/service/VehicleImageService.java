package com.vehicleplatform.service;

import com.vehicleplatform.adapter.ProviderException;
import com.vehicleplatform.adapter.carsxe.CarsXeProvider;
import com.vehicleplatform.entity.Vehicle;
import com.vehicleplatform.entity.VehicleSourceData;
import com.vehicleplatform.repository.VehicleRepository;
import com.vehicleplatform.repository.VehicleSourceDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * Lazily resolves a real vehicle photo via CarsXE (see CarsXeProvider) the
 * first time a vehicle without one is viewed, then persists it on the
 * Vehicle row so every later request is a plain DB read — no repeated calls
 * against a metered API key. Cached a second time (30 days) at the HTTP
 * layer via CacheService, so even the very first lookup for a given
 * make/model/year/trim combination is never repeated across vehicles that
 * happen to share one (e.g. two trims of the same model/year).
 */
@Slf4j
@Service
public class VehicleImageService {

    private static final long CACHE_TTL_HOURS = 24L * 30;

    private final CarsXeProvider carsXeProvider;
    private final CacheService cacheService;
    private final VehicleRepository vehicleRepository;
    private final VehicleSourceDataRepository sourceDataRepository;

    public VehicleImageService(CarsXeProvider carsXeProvider, CacheService cacheService,
                                VehicleRepository vehicleRepository, VehicleSourceDataRepository sourceDataRepository) {
        this.carsXeProvider = carsXeProvider;
        this.cacheService = cacheService;
        this.vehicleRepository = vehicleRepository;
        this.sourceDataRepository = sourceDataRepository;
    }

    /** Returns the vehicle's image URL, fetching and persisting one via CarsXE if missing. */
    @Transactional
    public Optional<String> ensureImage(Vehicle detachedVehicle) {
        if (detachedVehicle.getImageUrl() != null) {
            return Optional.of(detachedVehicle.getImageUrl());
        }
        if (!carsXeProvider.isConfigured()) {
            return Optional.empty();
        }

        Vehicle vehicle = vehicleRepository.findById(detachedVehicle.getId()).orElse(null);
        if (vehicle == null || vehicle.getImageUrl() != null) {
            return vehicle != null ? Optional.ofNullable(vehicle.getImageUrl()) : Optional.empty();
        }

        String make = vehicle.getMake();
        String model = vehicle.getModel();
        Integer year = vehicle.getYear();
        String trim = vehicle.getTrim();

        try {
            var outcome = cacheService.getOrFetch(
                    CarsXeProvider.NAME,
                    "images",
                    Map.of("make", make, "model", model, "year", String.valueOf(year), "trim", trim == null ? "" : trim),
                    () -> carsXeProvider.findBestImage(make, model, year, trim)
                            .<Map<String, Object>>map(img -> Map.of(
                                    "link", img.link() != null ? img.link() : "",
                                    "contextLink", img.contextLink() != null ? img.contextLink() : ""
                            ))
                            .orElseGet(() -> Map.of("link", "", "contextLink", "")),
                    CACHE_TTL_HOURS
            );

            String link = (String) outcome.payload().get("link");
            if (link == null || link.isBlank()) {
                return Optional.empty();
            }

            vehicle.setImageUrl(link);
            vehicleRepository.save(vehicle);
            sourceDataRepository.save(VehicleSourceData.builder()
                    .vehicle(vehicle)
                    .fieldName("image_url")
                    .fieldValue(link)
                    .sourceProvider(CarsXeProvider.NAME)
                    .sourceEndpoint("https://api.carsxe.com/images")
                    .demoData(false)
                    .retrievedAt(Instant.now())
                    .build());
            return Optional.of(link);
        } catch (ProviderException exc) {
            log.warn("CarsXE image lookup failed for {} {} {}: {}", make, model, year, exc.getMessage());
            return Optional.empty();
        }
    }
}
