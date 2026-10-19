package com.vehicleplatform.dto.comparison;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record SavedComparisonCreateRequest(
        @NotBlank @Size(min = 1, max = 150) String name,
        @Size(min = 2, max = 4) List<UUID> vehicleIds
) {
}
