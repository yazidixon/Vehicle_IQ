package com.vehicleplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vin_decodes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VinDecode {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 17)
    private String vin;

    private String countryOfManufacture;
    private String manufacturer;
    private String plant;
    private String modelYear;
    private String make;
    private String model;
    private String series;
    private String trim;
    private String engineCode;
    private String bodyClass;

    @Column(columnDefinition = "TEXT")
    private String rawPayload;

    @Column(nullable = false)
    @Builder.Default
    private String sourceProvider = "nhtsa";

    @Column(nullable = false)
    @Builder.Default
    private boolean demoData = false;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
}
