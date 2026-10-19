package com.vehicleplatform.service;

import com.vehicleplatform.dto.comparison.ComparisonMetricRow;
import com.vehicleplatform.dto.comparison.ComparisonResult;
import com.vehicleplatform.dto.vehicle.VehicleResponse;
import com.vehicleplatform.entity.Vehicle;
import com.vehicleplatform.entity.VehicleSpecification;
import com.vehicleplatform.exception.ApiException;
import com.vehicleplatform.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class ComparisonService {

    private final VehicleRepository vehicleRepository;

    public ComparisonService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    private record MetricDefinition(String key, String label, String unit, String betterDirection,
                                     Function<Vehicle, Object> accessor) {
    }

    private static VehicleSpecification spec(Vehicle v) {
        return v.getSpecification();
    }

    private final List<MetricDefinition> metrics = List.of(
            new MetricDefinition("horsepower_hp", "Puissance", "ch", "higher",
                    v -> spec(v) != null ? spec(v).getHorsepowerHp() : null),
            new MetricDefinition("torque_nm", "Couple", "Nm", "higher",
                    v -> spec(v) != null ? spec(v).getTorqueNm() : null),
            new MetricDefinition("acceleration_0_100_s", "0-100 km/h", "s", "lower",
                    v -> spec(v) != null ? spec(v).getAcceleration0100S() : null),
            new MetricDefinition("top_speed_kmh", "Vitesse max", "km/h", "higher",
                    v -> spec(v) != null ? spec(v).getTopSpeedKmh() : null),
            new MetricDefinition("fuel_consumption_mixed_l100km", "Consommation mixte", "L/100km", "lower",
                    v -> spec(v) != null ? spec(v).getFuelConsumptionMixedL100km() : null),
            new MetricDefinition("co2_emissions_gkm", "Émissions CO₂", "g/km", "lower",
                    v -> spec(v) != null ? spec(v).getCo2EmissionsGkm() : null),
            new MetricDefinition("weight_kg", "Poids", "kg", "lower",
                    v -> spec(v) != null ? spec(v).getWeightKg() : null),
            new MetricDefinition("length_mm", "Longueur", "mm", "neutral",
                    v -> spec(v) != null ? spec(v).getLengthMm() : null),
            new MetricDefinition("wheelbase_mm", "Empattement", "mm", "neutral",
                    v -> spec(v) != null ? spec(v).getWheelbaseMm() : null),
            new MetricDefinition("doors", "Portes", null, "neutral",
                    v -> spec(v) != null ? spec(v).getDoors() : null),
            new MetricDefinition("seats", "Places", null, "neutral",
                    v -> spec(v) != null ? spec(v).getSeats() : null),
            new MetricDefinition("price_msrp", "Prix (indicatif)", "€", "lower",
                    v -> spec(v) != null ? spec(v).getPriceMsrp() : null)
    );

    @Transactional(readOnly = true)
    public ComparisonResult compare(List<UUID> vehicleIds) {
        List<Vehicle> vehicles = vehicleRepository.findAllById(vehicleIds);
        if (vehicles.size() != vehicleIds.stream().distinct().count()) {
            throw ApiException.notFound("Un ou plusieurs véhicules sont introuvables.");
        }
        Map<UUID, Vehicle> byId = new HashMap<>();
        vehicles.forEach(v -> byId.put(v.getId(), v));
        List<Vehicle> ordered = vehicleIds.stream().map(byId::get).toList();
        ordered.forEach(v -> {
            if (v.getSpecification() != null) v.getSpecification().getEquipment().size();
        });

        List<ComparisonMetricRow> rows = new ArrayList<>();
        for (MetricDefinition metric : metrics) {
            Map<String, Object> values = new LinkedHashMap<>();
            Map<String, Double> numericValues = new LinkedHashMap<>();
            for (Vehicle v : ordered) {
                Object raw = metric.accessor().apply(v);
                values.put(v.getId().toString(), raw);
                if (raw instanceof Number number) {
                    numericValues.put(v.getId().toString(), number.doubleValue());
                }
            }

            String bestId = null;
            if (!numericValues.isEmpty() && !metric.betterDirection().equals("neutral")) {
                var comparator = metric.betterDirection().equals("higher")
                        ? Map.Entry.<String, Double>comparingByValue()
                        : Map.Entry.<String, Double>comparingByValue().reversed();
                bestId = numericValues.entrySet().stream().max(comparator).map(Map.Entry::getKey).orElse(null);
            }

            rows.add(new ComparisonMetricRow(metric.key(), metric.label(), metric.unit(), metric.betterDirection(),
                    values, bestId));
        }

        return new ComparisonResult(ordered.stream().map(VehicleResponse::from).toList(), rows);
    }
}
