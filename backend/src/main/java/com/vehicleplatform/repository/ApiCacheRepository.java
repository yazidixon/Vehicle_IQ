package com.vehicleplatform.repository;

import com.vehicleplatform.entity.ApiCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiCacheRepository extends JpaRepository<ApiCache, UUID> {

    // findFirstBy...OrderByCreatedAtDesc (not a plain findBy...Optional<ApiCache>):
    // a race between two concurrent cache-miss requests for the same key can
    // insert two rows before either commits, and a single-result query then
    // throws NonUniqueResultException on the next lookup instead of just
    // returning the newest entry. findFirst tolerates duplicates fine.
    Optional<ApiCache> findFirstByProviderAndEndpointAndParamsHashOrderByCreatedAtDesc(
            String provider, String endpoint, String paramsHash);

    List<ApiCache> findByExpiresAtBefore(Instant now);
}
