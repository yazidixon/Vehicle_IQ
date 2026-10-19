package com.vehicleplatform.dto.comparison;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SavedComparisonUpdateRequest(
        @NotBlank @Size(min = 1, max = 150) String name
) {
}
