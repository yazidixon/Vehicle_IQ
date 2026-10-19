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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "vehicle_specifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleSpecification {

    @Id
    @UuidGenerator
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false, unique = true)
    private Vehicle vehicle;

    // Motorisation
    private String engineType;
    private Integer engineDisplacementCc;
    private Integer horsepowerHp;
    private Integer torqueNm;
    private String transmission;
    private String drivetrain;

    // Performances
    private Integer topSpeedKmh;
    private Double acceleration0100S;

    // Dimensions
    private Integer lengthMm;
    private Integer widthMm;
    private Integer heightMm;
    private Integer wheelbaseMm;
    private Integer weightKg;
    private Integer doors;
    private Integer seats;

    // Consommation / environnement
    private Double fuelConsumptionUrbanL100km;
    private Double fuelConsumptionExtraUrbanL100km;
    private Double fuelConsumptionMixedL100km;
    private Integer co2EmissionsGkm;
    private String euroNorm;
    private String energyClass;

    // Prix
    private Double priceMsrp;
    private String currency;

    @ElementCollection
    @CollectionTable(name = "vehicle_specification_equipment", joinColumns = @JoinColumn(name = "specification_id"))
    @Column(name = "equipment_item")
    @Builder.Default
    private List<String> equipment = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String safetyNotes;

    @Column(nullable = false)
    @Builder.Default
    private boolean demoData = false;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
