package com.vehicleplatform.dto.vehicle;

import com.vehicleplatform.entity.Vehicle;

import java.util.UUID;

public record VehicleSummaryResponse(
        UUID id,
        String manufacturer,
        String make,
        String model,
        Integer year,
        String trim,
        String fuelType,
        Integer horsepowerHp,
        String transmission,
        String imageUrl,
        boolean isDemoData
) {
    public static VehicleSummaryResponse from(Vehicle v) {
        var spec = v.getSpecification();
        return new VehicleSummaryResponse(
                v.getId(), v.getManufacturer(), v.getMake(), v.getModel(), v.getYear(), v.getTrim(),
                v.getFuelType(), spec != null ? spec.getHorsepowerHp() : null,
                spec != null ? spec.getTransmission() : null, v.getImageUrl(), v.isDemoData()
        );
    }
}
