package com.vehicleplatform.adapter.carsxe;

import com.vehicleplatform.adapter.ProviderException;
import com.vehicleplatform.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Real integration with the CarsXE API (https://carsxe.com), used with a
 * paid/trial API key supplied by the user (see .env.example: CARSXE_API_KEY).
 * Verified live against the published OpenAPI schema at
 * https://docs.carsxe.com/api/openapi on 2026-09-13 — endpoints, parameter
 * names and response shapes below match that schema, not a guess.
 *
 * Used for two purposes only (no invented endpoints):
 *  - /images   : real vehicle photos by make/model/year/trim.
 *  - /specs    : VIN-based enrichment (equipment, colors, warranties, engine
 *                text) layered on top of the NHTSA decode, never replacing it.
 */
@Slf4j
@Component
public class CarsXeProvider {

    public static final String NAME = "carsxe";
    private static final String BASE_URL = "https://api.carsxe.com";

    private final AppProperties properties;
    private final RestClient restClient;

    public CarsXeProvider(AppProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder().build();
    }

    public boolean isConfigured() {
        return properties.providers() != null && properties.providers().carsxeKey() != null
                && !properties.providers().carsxeKey().isBlank();
    }

    public record ImageResult(String link, String thumbnailLink, Integer width, Integer height, String contextLink) {
    }

    /**
     * GET /images — real photos by make/model/year/trim. Our internal "trim"
     * values are often powertrain badges (e.g. "1.6 T-GDI HEV") rather than
     * the marketing trim names CarsXE's image index recognizes, so an exact
     * make+model+year+trim query frequently 404s even though the vehicle
     * itself has photos. We progressively relax the query (drop trim, then
     * year) rather than giving up on the first miss — stopping only for
     * auth/quota errors (401/429), which no amount of retrying will fix.
     */
    public Optional<ImageResult> findBestImage(String make, String model, Integer year, String trim) {
        // CarsXE's image index matches on plain ASCII names ("Skoda" works,
        // "Škoda" 404s even with everything else identical — verified live).
        String asciiMake = stripDiacritics(make);
        String asciiModel = stripDiacritics(model);

        List<Map<String, String>> attempts = new java.util.ArrayList<>();
        if (trim != null) attempts.add(paramsOf(asciiMake, asciiModel, year, trim));
        if (year != null) attempts.add(paramsOf(asciiMake, asciiModel, year, null));
        attempts.add(paramsOf(asciiMake, asciiModel, null, null));

        for (Map<String, String> params : attempts) {
            try {
                Optional<ImageResult> result = fetchImages(params);
                if (result.isPresent()) return result;
            } catch (ProviderException exc) {
                if (exc.getStatusCode() != null && (exc.getStatusCode() == 401 || exc.getStatusCode() == 429)) {
                    throw exc;
                }
                // 404 / 500 / "no match" for this attempt: fall through to the next, looser query.
            }
        }
        return Optional.empty();
    }

    private String stripDiacritics(String value) {
        if (value == null) return null;
        String normalized = java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }

    private Map<String, String> paramsOf(String make, String model, Integer year, String trim) {
        Map<String, String> params = new java.util.LinkedHashMap<>();
        params.put("make", make);
        params.put("model", model);
        if (year != null) params.put("year", String.valueOf(year));
        if (trim != null) params.put("trim", trim);
        return params;
    }

    @SuppressWarnings("unchecked")
    private Optional<ImageResult> fetchImages(Map<String, String> params) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BASE_URL + "/images")
                .queryParam("key", properties.providers().carsxeKey())
                .queryParam("size", "Large")
                .queryParam("transparent", "false");
        params.forEach(builder::queryParam);
        String url = builder.toUriString();

        Map<String, Object> body = get(url);
        if (!Boolean.TRUE.equals(body.get("success"))) {
            return Optional.empty();
        }
        List<Map<String, Object>> images = (List<Map<String, Object>>) body.getOrDefault("images", List.of());
        if (images.isEmpty()) {
            return Optional.empty();
        }
        Map<String, Object> first = images.get(0);
        return Optional.of(new ImageResult(
                (String) first.get("link"),
                (String) first.get("thumbnailLink"),
                asInteger(first.get("width")),
                asInteger(first.get("height")),
                (String) first.get("contextLink")
        ));
    }

    public record SpecsEnrichment(
            Map<String, Object> attributes, Map<String, Object> equipment,
            List<Map<String, Object>> colors, List<Map<String, Object>> warranties
    ) {
    }

    /** GET /specs — VIN-based enrichment layered on top of the NHTSA decode. */
    @SuppressWarnings("unchecked")
    public Optional<SpecsEnrichment> decodeVinEnrichment(String vin) {
        String url = UriComponentsBuilder.fromUriString(BASE_URL + "/specs")
                .queryParam("key", properties.providers().carsxeKey())
                .queryParam("vin", vin)
                .toUriString();
        try {
            Map<String, Object> body = get(url);
            if (!Boolean.TRUE.equals(body.get("success"))) {
                return Optional.empty();
            }
            return Optional.of(new SpecsEnrichment(
                    (Map<String, Object>) body.getOrDefault("attributes", Map.of()),
                    (Map<String, Object>) body.getOrDefault("equipment", Map.of()),
                    (List<Map<String, Object>>) body.getOrDefault("colors", List.of()),
                    (List<Map<String, Object>>) body.getOrDefault("warranties", List.of())
            ));
        } catch (ProviderException exc) {
            log.warn("CarsXE specs enrichment unavailable for VIN {}: {}", vin, exc.getMessage());
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> get(String url) {
        try {
            Map<String, Object> body = restClient.get().uri(url).retrieve().body(Map.class);
            return body != null ? body : Map.of();
        } catch (RestClientResponseException exc) {
            log.warn("CarsXE HTTP error {} for url={}", exc.getStatusCode(), safeUrl(url));
            throw new ProviderException(NAME, "Upstream HTTP error", exc.getStatusCode().value());
        } catch (RestClientException exc) {
            log.warn("CarsXE network/timeout error: {}", exc.getMessage());
            throw new ProviderException(NAME, "Network error or timeout contacting CarsXE");
        }
    }

    private Integer asInteger(Object value) {
        if (value instanceof Number number) return number.intValue();
        return null;
    }

    private String safeUrl(String url) {
        return url.replaceAll("key=[^&]+", "key=***");
    }
}
