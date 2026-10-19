package com.vehicleplatform.dto.vehicle;

public record VehicleSearchFilters(
        String q,
        String make,
        String model,
        String manufacturer,
        Integer year,
        Integer yearMin,
        Integer yearMax,
        String fuelType,
        String engineCode,
        int page,
        int pageSize
) {
}
