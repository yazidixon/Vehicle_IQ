package com.vehicleplatform.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Spring Boot 4 defaults to Jackson 3 (tools.jackson.*) for the autoconfigured
 * ObjectMapper bean used by the web layer. This utility intentionally owns a
 * separate, plain Jackson 2 ObjectMapper for internal persistence-layer JSON
 * (cache payloads, VIN raw payloads, search history params) so it never
 * depends on which Jackson major version Spring's web stack is wired to.
 */
@Component
public class JsonUtil {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exc) {
            throw new IllegalStateException("Unable to serialize value to JSON", exc);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> toMap(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception exc) {
            return Map.of();
        }
    }

    public <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception exc) {
            throw new IllegalStateException("Unable to deserialize JSON", exc);
        }
    }
}
