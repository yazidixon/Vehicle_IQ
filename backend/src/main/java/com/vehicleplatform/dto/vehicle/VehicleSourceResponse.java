package com.vehicleplatform.dto.vehicle;

import com.vehicleplatform.entity.VehicleSourceData;

import java.time.Instant;

public record VehicleSourceResponse(
        String fieldName,
        String fieldValue,
        String sourceProvider,
        String sourceEndpoint,
        boolean isDemoData,
        Instant retrievedAt
) {
    public static VehicleSourceResponse from(VehicleSourceData s) {
        return new VehicleSourceResponse(
                s.getFieldName(), s.getFieldValue(), s.getSourceProvider(), s.getSourceEndpoint(),
                s.isDemoData(), s.getRetrievedAt()
        );
    }
}
