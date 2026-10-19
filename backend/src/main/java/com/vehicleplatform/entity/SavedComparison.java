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
@Table(name = "saved_comparisons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedComparison {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    // EAGER: this list is read on every listing/serialization and open-in-view
    // is disabled, so a LAZY collection would throw LazyInitializationException
    // once the Hibernate session closes at the end of the transaction.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "saved_comparison_vehicles", joinColumns = @JoinColumn(name = "comparison_id"))
    @Column(name = "vehicle_id")
    @OrderColumn(name = "position")
    @Builder.Default
    private List<UUID> vehicleIds = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
