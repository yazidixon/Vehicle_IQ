package com.vehicleplatform.adapter;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/** Common shape every VIN-decoding provider must return. Never fabricate a missing field. */
@Getter
@Builder
public class NormalizedVinDecode {
    private final String vin;
    private final boolean valid;
    private final String countryOfManufacture;
    private final String manufacturer;
    private final String plant;
    private final String modelYear;
    private final String make;
    private final String model;
    private final String series;
    private final String trim;
    private final String engineCode;
    private final String bodyClass;
    @Builder.Default
    private final Map<String, Object> rawPayload = Map.of();
    private final String sourceProvider;
    private final String sourceEndpoint;
    @Builder.Default
    private final boolean demoData = false;
    @Builder.Default
    private final List<String> warnings = List.of();
}
