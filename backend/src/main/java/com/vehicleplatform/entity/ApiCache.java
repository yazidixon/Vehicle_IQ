package com.vehicleplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "api_cache",
        indexes = @Index(name = "ix_api_cache_lookup", columnList = "provider,endpoint,paramsHash")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiCache {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String endpoint;

    @Column(nullable = false, length = 64)
    private String paramsHash;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String params;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String response;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;
}
