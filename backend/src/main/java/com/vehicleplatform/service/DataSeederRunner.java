package com.vehicleplatform.service;

import com.vehicleplatform.adapter.demo.DemoProvider;
import com.vehicleplatform.adapter.demo.DemoVehicleRow;
import com.vehicleplatform.config.AppProperties;
import com.vehicleplatform.entity.ApiSource;
import com.vehicleplatform.entity.Vehicle;
import com.vehicleplatform.entity.VehicleSourceData;
import com.vehicleplatform.entity.VehicleSpecification;
import com.vehicleplatform.repository.ApiSourceRepository;
import com.vehicleplatform.repository.VehicleRepository;
import com.vehicleplatform.repository.VehicleSourceDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Seeds, on every startup:
 *  - the API source registry (real + studied-but-excluded providers)
 *  - the curated demo vehicle catalog (Vehicle + VehicleSpecification + VehicleSourceData)
 * Idempotent: skips vehicles/sources that already exist.
 */
@Slf4j
@Component
public class DataSeederRunner implements CommandLineRunner {

    private final ApiSourceRepository apiSourceRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleSourceDataRepository sourceDataRepository;
    private final DemoProvider demoProvider;
    private final AppProperties appProperties;

    public DataSeederRunner(ApiSourceRepository apiSourceRepository, VehicleRepository vehicleRepository,
                             VehicleSourceDataRepository sourceDataRepository, DemoProvider demoProvider,
                             AppProperties appProperties) {
        this.apiSourceRepository = apiSourceRepository;
        this.vehicleRepository = vehicleRepository;
        this.sourceDataRepository = sourceDataRepository;
        this.demoProvider = demoProvider;
        this.appProperties = appProperties;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedApiSources();
        seedDemoVehicles();
    }

    private void upsertSource(String name, String baseUrl, String description, boolean requiresKey, boolean configured) {
        ApiSource source = apiSourceRepository.findByName(name).orElseGet(() -> ApiSource.builder().name(name).build());
        source.setBaseUrl(baseUrl);
        source.setDescription(description);
        source.setRequiresKey(requiresKey);
        source.setConfigured(configured);
        source.setActive(true);
        apiSourceRepository.save(source);
    }

    private void seedApiSources() {
        var providers = appProperties.providers();
        upsertSource("nhtsa", "https://vpic.nhtsa.dot.gov/api/vehicles",
                "NHTSA vPIC — API publique et gratuite (sans clé) du gouvernement américain. " +
                        "Utilisée pour le décodage VIN et les listes marque/modèle.",
                false, true);
        upsertSource("demo", "local://src/main/resources/data/demo_vehicles.json",
                "Jeu de données local utilisé pour les spécifications techniques complètes " +
                        "(puissance, couple, consommation, CO2...) en mode démonstration.",
                false, true);
        upsertSource("carquery", "https://www.carqueryapi.com",
                "Étudiée mais non intégrée : service historiquement instable, hors ligne au moment de " +
                        "l'étude (voir docs/api-comparison.md).",
                false, providers != null && !isBlank(providers.carqueryUrl()));
        upsertSource("api_ninjas", "https://api.api-ninjas.com/v1/cars",
                "Intégrée : recherche live (make/model/année, transmission, cylindrée, carburant) " +
                        "utilisée en secours quand le catalogue local ne contient pas le véhicule demandé " +
                        "(clé gratuite — l'endpoint v2 complet reste payant, voir docs/api-comparison.md).",
                true, providers != null && !isBlank(providers.apiNinjasKey()));
        upsertSource("marketcheck", "https://www.marketcheck.com",
                "Étudiée mais non intégrée en V1 : compte et clé payante requis au-delà d'un quota " +
                        "d'essai limité (voir docs/api-comparison.md).",
                true, providers != null && !isBlank(providers.marketcheckKey()));
        upsertSource("carsxe", "https://api.carsxe.com",
                "Intégrée : photos réelles de véhicules (endpoint /images) et enrichissement VIN " +
                        "(endpoint /specs) via une clé fournie par l'utilisateur (voir docs/api-comparison.md).",
                true, providers != null && !isBlank(providers.carsxeKey()));
        log.info("API source registry seeded.");
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private void seedDemoVehicles() {
        int created = 0;
        for (DemoVehicleRow row : demoProvider.rows()) {
            boolean exists = vehicleRepository
                    .existsByMakeIgnoreCaseAndModelIgnoreCaseAndYear(row.make(), row.model(), row.year());
            if (exists) continue;

            Vehicle vehicle = Vehicle.builder()
                    .manufacturer(row.manufacturer()).make(row.make()).model(row.model())
                    .generation(row.generation()).year(row.year()).trim(row.trim())
                    .bodyType(row.bodyType()).fuelType(row.fuelType()).engineCode(row.engineCode())
                    .sourceProvider("demo").demoData(true)
                    .build();
            vehicle = vehicleRepository.save(vehicle);

            VehicleSpecification spec = VehicleSpecification.builder()
                    .vehicle(vehicle)
                    .engineType(row.engineType()).engineDisplacementCc(row.engineDisplacementCc())
                    .horsepowerHp(row.horsepowerHp()).torqueNm(row.torqueNm())
                    .transmission(row.transmission()).drivetrain(row.drivetrain())
                    .topSpeedKmh(row.topSpeedKmh()).acceleration0100S(row.acceleration0100S())
                    .lengthMm(row.lengthMm()).widthMm(row.widthMm()).heightMm(row.heightMm())
                    .wheelbaseMm(row.wheelbaseMm()).weightKg(row.weightKg())
                    .doors(row.doors()).seats(row.seats())
                    .fuelConsumptionUrbanL100km(row.fuelConsumptionUrbanL100km())
                    .fuelConsumptionExtraUrbanL100km(row.fuelConsumptionExtraUrbanL100km())
                    .fuelConsumptionMixedL100km(row.fuelConsumptionMixedL100km())
                    .co2EmissionsGkm(row.co2EmissionsGkm()).euroNorm(row.euroNorm()).energyClass(row.energyClass())
                    .priceMsrp(row.priceMsrp()).currency(row.currency())
                    .equipment(row.equipment() != null ? new java.util.ArrayList<>(row.equipment()) : new java.util.ArrayList<>())
                    .safetyNotes(row.safetyNotes())
                    .demoData(true)
                    .build();
            vehicle.setSpecification(spec);
            vehicleRepository.save(vehicle);

            recordSourceData(vehicle, spec);
            created++;
        }
        log.info("Demo vehicle seed complete: {} new vehicle(s) inserted.", created);
    }

    private void recordSourceData(Vehicle vehicle, VehicleSpecification spec) {
        Instant now = Instant.now();
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("horsepower_hp", spec.getHorsepowerHp());
        fields.put("torque_nm", spec.getTorqueNm());
        fields.put("fuel_consumption_mixed_l100km", spec.getFuelConsumptionMixedL100km());
        fields.put("co2_emissions_gkm", spec.getCo2EmissionsGkm());
        fields.put("weight_kg", spec.getWeightKg());
        fields.put("price_msrp", spec.getPriceMsrp());

        fields.forEach((fieldName, value) -> {
            if (value == null) return;
            sourceDataRepository.save(VehicleSourceData.builder()
                    .vehicle(vehicle)
                    .fieldName(fieldName)
                    .fieldValue(String.valueOf(value))
                    .sourceProvider("demo")
                    .sourceEndpoint("local://src/main/resources/data/demo_vehicles.json")
                    .demoData(true)
                    .retrievedAt(now)
                    .build());
        });
    }
}
