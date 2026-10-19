package com.vehicleplatform.adapter.demo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Mirrors the field names in resources/data/demo_vehicles.json (curated,
 * clearly-labeled demo dataset — see docs/api-comparison.md for why no free
 * keyless full-spec API exists). Jackson's global snake_case naming strategy
 * matches every field here to its JSON key automatically, except the one
 * field with embedded digits, which is mapped explicitly.
 */
public record DemoVehicleRow(
        String manufacturer,
        String make,
        String model,
        String generation,
        Integer year,
        String trim,
        String bodyType,
        String fuelType,
        String engineCode,
        String engineType,
        Integer engineDisplacementCc,
        Integer horsepowerHp,
        Integer torqueNm,
        String transmission,
        String drivetrain,
        Integer topSpeedKmh,
        @JsonProperty("acceleration_0_100_s") Double acceleration0100S,
        Integer lengthMm,
        Integer widthMm,
        Integer heightMm,
        Integer wheelbaseMm,
        Integer weightKg,
        Integer doors,
        Integer seats,
        Double fuelConsumptionUrbanL100km,
        Double fuelConsumptionExtraUrbanL100km,
        Double fuelConsumptionMixedL100km,
        Integer co2EmissionsGkm,
        String euroNorm,
        String energyClass,
        Double priceMsrp,
        String currency,
        List<String> equipment,
        String safetyNotes
) {
}
