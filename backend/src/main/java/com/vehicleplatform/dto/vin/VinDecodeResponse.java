package com.vehicleplatform.dto.vin;

import java.util.List;

public record VinDecodeResponse(
        String vin,
        boolean isValid,
        String countryOfManufacture,
        String manufacturer,
        String plant,
        String modelYear,
        String make,
        String model,
        String series,
        String trim,
        String engineCode,
        String bodyClass,
        String sourceProvider,
        boolean isDemoData,
        List<String> warnings
) {
}
