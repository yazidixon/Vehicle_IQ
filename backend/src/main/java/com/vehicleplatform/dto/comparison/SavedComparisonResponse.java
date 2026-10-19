package com.vehicleplatform.dto.comparison;

import com.vehicleplatform.entity.SavedComparison;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SavedComparisonResponse(
        UUID id,
        String name,
        List<UUID> vehicleIds,
        Instant createdAt,
        Instant updatedAt
) {
    public static SavedComparisonResponse from(SavedComparison c) {
        return new SavedComparisonResponse(c.getId(), c.getName(), c.getVehicleIds(), c.getCreatedAt(), c.getUpdatedAt());
    }
}
