package com.vehicleplatform.adapter;

import lombok.Getter;

@Getter
public class ProviderException extends RuntimeException {
    private final String provider;
    private final Integer statusCode;

    public ProviderException(String provider, String message) {
        this(provider, message, null);
    }

    public ProviderException(String provider, String message, Integer statusCode) {
        super("[" + provider + "] " + message);
        this.provider = provider;
        this.statusCode = statusCode;
    }
}
