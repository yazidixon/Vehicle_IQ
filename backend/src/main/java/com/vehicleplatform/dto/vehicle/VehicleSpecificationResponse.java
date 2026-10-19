package com.vehicleplatform.dto.vehicle;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vehicleplatform.entity.VehicleSpecification;

import java.util.List;

public record VehicleSpecificationResponse(
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
        String safetyNotes,
        boolean isDemoData
) {
    public static VehicleSpecificationResponse from(VehicleSpecification s) {
        if (s == null) return null;
        return new VehicleSpecificationResponse(
                s.getEngineType(), s.getEngineDisplacementCc(), s.getHorsepowerHp(), s.getTorqueNm(),
                s.getTransmission(), s.getDrivetrain(), s.getTopSpeedKmh(), s.getAcceleration0100S(),
                s.getLengthMm(), s.getWidthMm(), s.getHeightMm(), s.getWheelbaseMm(), s.getWeightKg(),
                s.getDoors(), s.getSeats(), s.getFuelConsumptionUrbanL100km(), s.getFuelConsumptionExtraUrbanL100km(),
                s.getFuelConsumptionMixedL100km(), s.getCo2EmissionsGkm(), s.getEuroNorm(), s.getEnergyClass(),
                s.getPriceMsrp(), s.getCurrency(), s.getEquipment(), s.getSafetyNotes(), s.isDemoData()
        );
    }
}
