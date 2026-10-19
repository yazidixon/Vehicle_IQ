package com.vehicleplatform.adapter;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/** Common normalized technical-specification shape used across providers. */
@Getter
@Builder
public class NormalizedVehicleSpec {
    private final String manufacturer;
    private final String make;
    private final String model;
    private final Integer year;
    private final String generation;
    private final String trim;
    private final String bodyType;
    private final String fuelType;
    private final String engineCode;
    private final String imageUrl;
    private final String externalId;

    private final String engineType;
    private final Integer engineDisplacementCc;
    private final Integer horsepowerHp;
    private final Integer torqueNm;
    private final String transmission;
    private final String drivetrain;

    private final Integer topSpeedKmh;
    private final Double acceleration0100S;

    private final Integer lengthMm;
    private final Integer widthMm;
    private final Integer heightMm;
    private final Integer wheelbaseMm;
    private final Integer weightKg;
    private final Integer doors;
    private final Integer seats;

    private final Double fuelConsumptionUrbanL100km;
    private final Double fuelConsumptionExtraUrbanL100km;
    private final Double fuelConsumptionMixedL100km;
    private final Integer co2EmissionsGkm;
    private final String euroNorm;
    private final String energyClass;

    private final Double priceMsrp;
    private final String currency;

    @Builder.Default
    private final List<String> equipment = List.of();
    private final String safetyNotes;

    @Builder.Default
    private final String sourceProvider = "demo";
    private final String sourceEndpoint;
    @Builder.Default
    private final boolean demoData = false;
}
