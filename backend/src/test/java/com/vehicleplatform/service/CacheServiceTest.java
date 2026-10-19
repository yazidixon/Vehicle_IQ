package com.vehicleplatform.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class CacheServiceTest {

    @Autowired
    private CacheService cacheService;

    @Test
    void cacheMissThenHitCallsFetcherOnlyOnce() {
        String provider = "test_provider_" + UUID.randomUUID();
        AtomicInteger calls = new AtomicInteger(0);

        var first = cacheService.getOrFetch(provider, "endpoint", Map.of("a", 1), () -> {
            calls.incrementAndGet();
            return Map.of("value", 42);
        });
        var second = cacheService.getOrFetch(provider, "endpoint", Map.of("a", 1), () -> {
            calls.incrementAndGet();
            return Map.of("value", 42);
        });

        assertThat(first.cacheHit()).isFalse();
        assertThat(second.cacheHit()).isTrue();
        assertThat(calls.get()).isEqualTo(1);
        assertThat(second.payload().get("value")).isEqualTo(42);
    }

    @Test
    void cacheIsScopedByParams() {
        String provider = "test_provider_" + UUID.randomUUID();

        var a = cacheService.getOrFetch(provider, "endpoint", Map.of("vin", "AAA"), () -> Map.of("echo", "AAA"));
        var b = cacheService.getOrFetch(provider, "endpoint", Map.of("vin", "BBB"), () -> Map.of("echo", "BBB"));

        assertThat(a.payload().get("echo")).isEqualTo("AAA");
        assertThat(b.payload().get("echo")).isEqualTo("BBB");
    }

    @Test
    void expiredCacheEntryTriggersRefetch() {
        String provider = "test_provider_" + UUID.randomUUID();
        cacheService.getOrFetch(provider, "endpoint", Map.of("a", 1), () -> Map.of("value", "stale"), -1L);

        var result = cacheService.getOrFetch(provider, "endpoint", Map.of("a", 1), () -> Map.of("value", "fresh"), 24L);

        assertThat(result.cacheHit()).isFalse();
        assertThat(result.payload().get("value")).isEqualTo("fresh");
    }
}
