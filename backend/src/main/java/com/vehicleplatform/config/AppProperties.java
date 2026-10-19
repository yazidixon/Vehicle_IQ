package com.vehicleplatform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        boolean demoMode,
        int cacheTtlHours,
        String corsOrigins,
        Jwt jwt,
        Nhtsa nhtsa,
        Providers providers
) {
    public List<String> corsOriginsList() {
        return corsOrigins == null || corsOrigins.isBlank()
                ? List.of()
                : List.of(corsOrigins.split(","));
    }

    public record Jwt(String secret, int expirationMinutes) {
    }

    public record Nhtsa(String baseUrl) {
    }

    public record Providers(
            String carqueryUrl,
            String apiNinjasKey,
            String marketcheckKey,
            String carsxeKey,
            String autodevKey,
            String edmundsKey
    ) {
    }
}
