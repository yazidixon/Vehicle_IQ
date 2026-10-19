package com.vehicleplatform.repository;

import com.vehicleplatform.entity.SavedComparison;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavedComparisonRepository extends JpaRepository<SavedComparison, UUID> {
    List<SavedComparison> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<SavedComparison> findByIdAndUserId(UUID id, UUID userId);

    long countByUserId(UUID userId);
}
