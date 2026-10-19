package com.vehicleplatform.adapter;

public interface VinDecodeProvider {
    String getName();

    boolean isConfigured();

    NormalizedVinDecode decode(String vin);
}
