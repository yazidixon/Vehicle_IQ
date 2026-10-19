package com.vehicleplatform.service;

import com.vehicleplatform.config.AppProperties;
import com.vehicleplatform.entity.ApiCache;
import com.vehicleplatform.repository.ApiCacheRepository;
import com.vehicleplatform.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Supplier;

/** Implements: cache lookup -> valid? -> return cached : call fetcher -> store -> return. */
@Slf4j
@Service
public class CacheService {

    private final ApiCacheRepository repository;
    private final JsonUtil jsonUtil;
    private final AppProperties appProperties;

    public CacheService(ApiCacheRepository repository, JsonUtil jsonUtil, AppProperties appProperties) {
        this.repository = repository;
        this.jsonUtil = jsonUtil;
        this.appProperties = appProperties;
    }

    public record CacheOutcome<T>(T payload, boolean cacheHit) {
    }

    private String hashParams(Map<String, Object> params) {
        try {
            String canonical = jsonUtil.toJson(new TreeMap<>(params));
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(canonical.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException exc) {
            throw new IllegalStateException(exc);
        }
    }

    public CacheOutcome<Map<String, Object>> getOrFetch(
            String provider, String endpoint, Map<String, Object> params, Supplier<Map<String, Object>> fetcher
    ) {
        return getOrFetch(provider, endpoint, params, fetcher, appProperties.cacheTtlHours());
    }

    public CacheOutcome<Map<String, Object>> getOrFetch(
            String provider, String endpoint, Map<String, Object> params, Supplier<Map<String, Object>> fetcher, long ttlHours
    ) {
        String paramsHash = hashParams(params);
        Optional<ApiCache> existing = repository
                .findFirstByProviderAndEndpointAndParamsHashOrderByCreatedAtDesc(provider, endpoint, paramsHash);

        if (existing.isPresent()) {
            ApiCache entry = existing.get();
            if (entry.getExpiresAt().isAfter(Instant.now())) {
                log.info("CACHE HIT provider={} endpoint={}", provider, endpoint);
                return new CacheOutcome<>(jsonUtil.toMap(entry.getResponse()), true);
            }
            repository.delete(entry);
        }

        log.info("CACHE MISS provider={} endpoint={} -> calling upstream", provider, endpoint);
        Map<String, Object> response = fetcher.get();

        ApiCache entry = ApiCache.builder()
                .provider(provider).endpoint(endpoint).paramsHash(paramsHash)
                .params(jsonUtil.toJson(params)).response(jsonUtil.toJson(response))
                .createdAt(Instant.now()).expiresAt(Instant.now().plus(ttlHours, ChronoUnit.HOURS))
                .build();
        repository.save(entry);

        return new CacheOutcome<>(response, false);
    }
}
