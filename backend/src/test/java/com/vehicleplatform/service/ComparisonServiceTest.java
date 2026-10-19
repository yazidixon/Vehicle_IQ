package com.vehicleplatform.service;

import com.vehicleplatform.entity.Vehicle;
import com.vehicleplatform.entity.VehicleSpecification;
import com.vehicleplatform.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional // single-threaded, in-process repository calls only — safe to roll back here
class ComparisonServiceTest {

    @Autowired
    private ComparisonService comparisonService;

    @Autowired
    private VehicleRepository vehicleRepository;

    private Vehicle seed(String name, Integer horsepower, Double consumption, Integer co2) {
        Vehicle vehicle = Vehicle.builder()
                .manufacturer(name).make(name).model("Model").year(2023)
                .fuelType("Essence").sourceProvider("demo").demoData(true).build();
        vehicle = vehicleRepository.save(vehicle);
        VehicleSpecification spec = VehicleSpecification.builder()
                .vehicle(vehicle).horsepowerHp(horsepower)
                .fuelConsumptionMixedL100km(consumption).co2EmissionsGkm(co2).demoData(true).build();
        vehicle.setSpecification(spec);
        return vehicleRepository.save(vehicle);
    }

    @Test
    void higherIsBetterForHorsepower() {
        Vehicle weak = seed("Weak", 90, 6.0, 140);
        Vehicle strong = seed("Strong", 300, 9.0, 210);

        var result = comparisonService.compare(List.of(weak.getId(), strong.getId()));

        var hpRow = result.metrics().stream().filter(m -> m.key().equals("horsepower_hp")).findFirst().orElseThrow();
        assertThat(hpRow.betterDirection()).isEqualTo("higher");
        assertThat(hpRow.bestVehicleId()).isEqualTo(strong.getId().toString());
    }

    @Test
    void lowerIsBetterForConsumptionAndCo2() {
        Vehicle efficient = seed("Efficient", 110, 4.0, 95);
        Vehicle thirsty = seed("Thirsty", 110, 11.0, 240);

        var result = comparisonService.compare(List.of(efficient.getId(), thirsty.getId()));

        var consumptionRow = result.metrics().stream().filter(m -> m.key().equals("fuel_consumption_mixed_l100km")).findFirst().orElseThrow();
        var co2Row = result.metrics().stream().filter(m -> m.key().equals("co2_emissions_gkm")).findFirst().orElseThrow();

        assertThat(consumptionRow.betterDirection()).isEqualTo("lower");
        assertThat(consumptionRow.bestVehicleId()).isEqualTo(efficient.getId().toString());
        assertThat(co2Row.bestVehicleId()).isEqualTo(efficient.getId().toString());
    }

    @Test
    void compareRequiresExistingVehicles() {
        List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID());
        assertThatThrownBy(() -> comparisonService.compare(ids))
                .isInstanceOf(com.vehicleplatform.exception.ApiException.class);
    }

    @Test
    void missingValuesDoNotCrashComparison() {
        Vehicle complete = seed("Complete", 150, 5.0, 120);
        Vehicle noSpecVehicle = Vehicle.builder()
                .manufacturer("NoSpec").make("NoSpec").model("Model").year(2023)
                .sourceProvider("demo").demoData(true).build();
        noSpecVehicle = vehicleRepository.save(noSpecVehicle);

        var result = comparisonService.compare(List.of(complete.getId(), noSpecVehicle.getId()));

        var hpRow = result.metrics().stream().filter(m -> m.key().equals("horsepower_hp")).findFirst().orElseThrow();
        assertThat(hpRow.values().get(noSpecVehicle.getId().toString())).isNull();
        assertThat(hpRow.bestVehicleId()).isEqualTo(complete.getId().toString());
    }
}
