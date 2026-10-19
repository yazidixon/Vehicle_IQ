package com.vehicleplatform.adapter;

import java.util.List;

public interface VehicleDataProvider {
    String getName();

    boolean isConfigured();

    List<NormalizedVehicleSpec> search(String make, String model, Integer year, String fuelType, String q);

    List<String> listMakes();

    List<String> listModels(String make);
}
