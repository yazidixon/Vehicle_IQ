package com.vehicleplatform.dto.comparison;

import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record ComparisonRequest(
        @Size(min = 2, max = 4) List<UUID> vehicleIds
) {
}
