package com.vehicleplatform.adapter.apininjas;

import com.vehicleplatform.adapter.NormalizedVehicleSpec;
import com.vehicleplatform.adapter.ProviderException;
import com.vehicleplatform.adapter.VehicleDataProvider;
import com.vehicleplatform.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Real integration with the API Ninjas Cars API (https://api-ninjas.com/api/cars),
 * using the legacy `/v1/cars` endpoint — the only one this project's free-tier
 * key can access (verified live: /v2/cars, /v2/carmakes etc. reject free keys
 * with "This endpoint is only available to premium subscribers"; /v1/cars
 * works but strips mpg fields the same way). Confirmed working 2026-09-13.
 *
 * Field coverage is intentionally partial (class, cylinders, displacement,
 * drive, fuel_type, transmission, year) — this is genuinely all the free
 * tier returns. No horsepower/torque/weight/price here; those fields stay
 * null rather than being guessed, exactly like every other provider.
 */
@Slf4j
@Component
public class ApiNinjasProvider implements VehicleDataProvider {

    public static final String NAME = "api_ninjas";
    private static final String BASE_URL = "https://api.api-ninjas.com/v1/cars";

    private final AppProperties properties;
    private final RestClient restClient;

    public ApiNinjasProvider(AppProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder().build();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isConfigured() {
        return properties.providers() != null && properties.providers().apiNinjasKey() != null
                && !properties.providers().apiNinjasKey().isBlank();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<NormalizedVehicleSpec> search(String make, String model, Integer year, String fuelType, String q) {
        if (make == null && model == null && q == null) {
            return List.of(); // API Ninjas requires at least one real filter
        }
        String effectiveMake = make != null ? make : q;

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BASE_URL);
        if (effectiveMake != null) builder.queryParam("make", effectiveMake);
        if (model != null) builder.queryParam("model", model);
        if (year != null) builder.queryParam("year", year);
        String url = builder.toUriString();

        List<Map<String, Object>> rows;
        try {
            Object body = restClient.get().uri(url)
                    .header("X-Api-Key", properties.providers().apiNinjasKey())
                    .retrieve().body(List.class);
            rows = (List<Map<String, Object>>) (List<?>) (body != null ? body : List.of());
        } catch (RestClientResponseException exc) {
            log.warn("API Ninjas HTTP error {} for make={} model={}", exc.getStatusCode(), effectiveMake, model);
            throw new ProviderException(NAME, "Upstream HTTP error", exc.getStatusCode().value());
        } catch (RestClientException exc) {
            log.warn("API Ninjas network/timeout error: {}", exc.getMessage());
            throw new ProviderException(NAME, "Network error or timeout contacting API Ninjas");
        }

        List<NormalizedVehicleSpec> results = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String rowFuelType = normalizeFuelType((String) row.get("fuel_type"));
            if (fuelType != null && (rowFuelType == null || !rowFuelType.equalsIgnoreCase(fuelType))) {
                continue;
            }
            results.add(toNormalized(row, url));
        }
        return results;
    }

    private NormalizedVehicleSpec toNormalized(Map<String, Object> row, String endpoint) {
        String rawMake = (String) row.get("make");
        String rawModel = (String) row.get("model");
        Object yearValue = row.get("year");

        return NormalizedVehicleSpec.builder()
                .manufacturer(capitalize(rawMake))
                .make(capitalize(rawMake))
                .model(capitalize(rawModel))
                .year(yearValue instanceof Number number ? number.intValue() : null)
                .bodyType(capitalize((String) row.get("class")))
                .fuelType(normalizeFuelType((String) row.get("fuel_type")))
                .engineDisplacementCc(toDisplacementCc(row.get("displacement")))
                .transmission(normalizeTransmission((String) row.get("transmission")))
                .drivetrain(row.get("drive") != null ? ((String) row.get("drive")).toUpperCase() : null)
                .sourceProvider(NAME)
                .sourceEndpoint(endpoint)
                .demoData(false)
                .build();
    }

    private Integer toDisplacementCc(Object displacementLiters) {
        if (displacementLiters instanceof Number number) {
            return Math.round(number.floatValue() * 1000);
        }
        return null;
    }

    private String normalizeFuelType(String raw) {
        if (raw == null) return null;
        return switch (raw.toLowerCase()) {
            case "gas" -> "Essence";
            case "diesel" -> "Diesel";
            default -> capitalize(raw);
        };
    }

    private String normalizeTransmission(String raw) {
        if (raw == null) return null;
        return switch (raw.toLowerCase()) {
            case "a" -> "Automatique";
            case "m" -> "Manuelle";
            default -> capitalize(raw);
        };
    }

    /** Title-cases every word: API Ninjas returns names in lowercase (e.g. "legacy awd turbo"). */
    private String capitalize(String value) {
        if (value == null || value.isBlank()) return value;
        String[] words = value.split(" ");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) result.append(' ');
            String word = words[i];
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
            }
        }
        return result.toString();
    }

    @Override
    public List<String> listMakes() {
        return List.of(); // no free-tier endpoint for this; NHTSA already covers autocomplete
    }

    @Override
    public List<String> listModels(String make) {
        return List.of();
    }
}
