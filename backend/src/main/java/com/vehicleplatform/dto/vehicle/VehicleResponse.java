package com.vehicleplatform.dto.vehicle;

import com.vehicleplatform.entity.Vehicle;

import java.util.UUID;

public record VehicleResponse(
        UUID id,
        String manufacturer,
        String make,
        String model,
        String generation,
        Integer year,
        String trim,
        String bodyType,
        String fuelType,
        String engineCode,
        String imageUrl,
        String sourceProvider,
        boolean isDemoData,
        VehicleSpecificationResponse specification
) {
    public static VehicleResponse from(Vehicle v) {
        return new VehicleResponse(
                v.getId(), v.getManufacturer(), v.getMake(), v.getModel(), v.getGeneration(), v.getYear(),
                v.getTrim(), v.getBodyType(), v.getFuelType(), v.getEngineCode(), v.getImageUrl(),
                v.getSourceProvider(), v.isDemoData(), VehicleSpecificationResponse.from(v.getSpecification())
        );
    }
}
