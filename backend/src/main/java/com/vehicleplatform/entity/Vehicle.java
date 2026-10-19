package com.vehicleplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String manufacturer;

    @Column(nullable = false)
    private String make;

    @Column(nullable = false)
    private String model;

    private String generation;

    // Mapped to a non-default column name: "year" is a reserved word in some
    // SQL dialects (e.g. H2's default mode), which breaks unquoted queries.
    @Column(name = "model_year", nullable = false)
    private Integer year;

    private String trim;
    private String bodyType;
    private String fuelType;
    private String engineCode;

    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false)
    @Builder.Default
    private String sourceProvider = "demo";

    private String externalId;

    @Column(nullable = false)
    @Builder.Default
    private boolean demoData = false;

    @OneToOne(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private VehicleSpecification specification;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
