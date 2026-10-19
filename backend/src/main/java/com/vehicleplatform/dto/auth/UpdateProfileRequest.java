package com.vehicleplatform.dto.auth;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 2, max = 255) String fullName,
        @Size(min = 8, max = 128) String password
) {
}
