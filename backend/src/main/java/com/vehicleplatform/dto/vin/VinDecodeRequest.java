package com.vehicleplatform.dto.vin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VinDecodeRequest(
        @NotBlank @Size(min = 17, max = 17) String vin
) {
}
