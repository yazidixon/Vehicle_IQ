package com.vehicleplatform.adapter.nhtsa;

import com.vehicleplatform.adapter.NormalizedVehicleSpec;
import com.vehicleplatform.adapter.NormalizedVinDecode;
import com.vehicleplatform.adapter.ProviderException;
import com.vehicleplatform.adapter.VehicleDataProvider;
import com.vehicleplatform.adapter.VinDecodeProvider;
import com.vehicleplatform.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Real integration with the NHTSA vPIC API (https://vpic.nhtsa.dot.gov/api/).
 * Free, public, key-less US government API. Used for VIN decoding and
 * make/model catalog lookups. It does NOT provide commercial specs
 * (horsepower, torque, consumption, CO2, price) — those are intentionally
 * left null here rather than fabricated. See docs/api-comparison.md.
 */
@Slf4j
@Component
public class NhtsaProvider implements VinDecodeProvider, VehicleDataProvider {

    public static final String NAME = "nhtsa";
    private static final Set<String> NOT_AVAILABLE_MARKERS = Set.of("", "not applicable", "n/a");

    private final AppProperties properties;
    private final RestClient restClient;

    public NhtsaProvider(AppProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder().build();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isConfigured() {
        return properties.nhtsa() != null && properties.nhtsa().baseUrl() != null
                && !properties.nhtsa().baseUrl().isBlank();
    }

    private String clean(Object value) {
        if (value == null) return null;
        String text = value.toString().trim();
        if (text.isEmpty() || NOT_AVAILABLE_MARKERS.contains(text.toLowerCase())) return null;
        return text;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> get(String url) {
        try {
            Map<String, Object> body = restClient.get().uri(url).retrieve().body(Map.class);
            return body != null ? body : Map.of();
        } catch (RestClientResponseException exc) {
            log.warn("NHTSA HTTP error {} for url={}", exc.getStatusCode(), url);
            throw new ProviderException(NAME, "Upstream HTTP error", exc.getStatusCode().value());
        } catch (RestClientException exc) {
            log.warn("NHTSA network/timeout error for url={}: {}", url, exc.getMessage());
            throw new ProviderException(NAME, "Network error or timeout contacting NHTSA");
        }
    }

    @Override
    public NormalizedVinDecode decode(String vin) {
        return normalize(vin, fetchRawVin(vin));
    }

    /** Raw upstream call — this is what gets cached (see VinService). */
    public Map<String, Object> fetchRawVin(String vin) {
        String url = properties.nhtsa().baseUrl() + "/DecodeVinValuesExtended/" + vin + "?format=json";
        Map<String, Object> payload = get(url);
        payload = new java.util.HashMap<>(payload);
        payload.put("__endpoint", url);
        return payload;
    }

    /** Pure normalization from a (possibly cached) raw NHTSA payload. Never fabricates missing fields. */
    @SuppressWarnings("unchecked")
    public NormalizedVinDecode normalize(String vin, Map<String, Object> payload) {
        String url = (String) payload.getOrDefault("__endpoint", properties.nhtsa().baseUrl());
        List<Map<String, Object>> results = (List<Map<String, Object>>) payload.getOrDefault("Results", List.of());
        Map<String, Object> row = results.isEmpty() ? Map.of() : results.get(0);

        String errorCode = clean(row.get("ErrorCode"));
        List<String> warnings = new ArrayList<>();
        boolean isValid = true;
        if (errorCode != null && !errorCode.equals("0")) {
            warnings.add(clean(row.get("ErrorText")) != null ? clean(row.get("ErrorText")) : "VIN could not be fully decoded.");
            if (Set.of("1", "6", "7", "8", "9", "14").contains(errorCode)) {
                isValid = false;
            }
        }

        String engineCode = clean(row.get("EngineConfiguration"));
        if (engineCode == null) engineCode = clean(row.get("EngineModel"));

        return NormalizedVinDecode.builder()
                .vin(vin)
                .valid(isValid)
                .countryOfManufacture(clean(row.get("PlantCountry")))
                .manufacturer(clean(row.get("Manufacturer")))
                .plant(clean(row.get("PlantCity")))
                .modelYear(clean(row.get("ModelYear")))
                .make(clean(row.get("Make")))
                .model(clean(row.get("Model")))
                .series(clean(row.get("Series")))
                .trim(clean(row.get("Trim")))
                .engineCode(engineCode)
                .bodyClass(clean(row.get("BodyClass")))
                .rawPayload(row)
                .sourceProvider(NAME)
                .sourceEndpoint(url)
                .demoData(false)
                .warnings(warnings)
                .build();
    }

    @Override
    public List<String> listMakes() {
        String url = properties.nhtsa().baseUrl() + "/GetAllMakes?format=json";
        Map<String, Object> payload = get(url);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> results = (List<Map<String, Object>>) payload.getOrDefault("Results", List.of());
        Set<String> makes = new TreeSet<>();
        for (Map<String, Object> r : results) {
            String name = clean(r.get("Make_Name"));
            if (name != null) makes.add(name);
        }
        return new ArrayList<>(makes);
    }

    @Override
    public List<String> listModels(String make) {
        String url = properties.nhtsa().baseUrl() + "/GetModelsForMake/" + make + "?format=json";
        Map<String, Object> payload = get(url);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> results = (List<Map<String, Object>>) payload.getOrDefault("Results", List.of());
        Set<String> models = new TreeSet<>();
        for (Map<String, Object> r : results) {
            String name = clean(r.get("Model_Name"));
            if (name != null) models.add(name);
        }
        return new ArrayList<>(models);
    }

    /** Identity-only search (no specs) used to seed real vehicle identities from NHTSA. */
    @Override
    public List<NormalizedVehicleSpec> search(String make, String model, Integer year, String fuelType, String q) {
        if (make == null || year == null) return List.of();
        String url = properties.nhtsa().baseUrl() + "/GetModelsForMakeYear/make/" + make + "/modelyear/" + year + "?format=json";
        Map<String, Object> payload = get(url);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> results = (List<Map<String, Object>>) payload.getOrDefault("Results", List.of());

        List<NormalizedVehicleSpec> specs = new ArrayList<>();
        for (Map<String, Object> row : results) {
            String modelName = clean(row.get("Model_Name"));
            if (modelName == null) continue;
            if (model != null && !modelName.toLowerCase().contains(model.toLowerCase())) continue;

            specs.add(NormalizedVehicleSpec.builder()
                    .manufacturer(clean(row.get("Make_Name")) != null ? clean(row.get("Make_Name")) : make)
                    .make(clean(row.get("Make_Name")) != null ? clean(row.get("Make_Name")) : make)
                    .model(modelName)
                    .year(year)
                    .externalId(row.get("Model_ID") != null ? row.get("Model_ID").toString() : null)
                    .sourceProvider(NAME)
                    .sourceEndpoint(properties.nhtsa().baseUrl() + "/GetModelsForMakeYear")
                    .demoData(false)
                    .build());
        }
        return specs;
    }
}
