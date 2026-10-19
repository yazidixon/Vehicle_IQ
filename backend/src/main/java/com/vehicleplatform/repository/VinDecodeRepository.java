package com.vehicleplatform.repository;

import com.vehicleplatform.entity.VinDecode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VinDecodeRepository extends JpaRepository<VinDecode, UUID> {
    List<VinDecode> findByUserIdOrderByCreatedAtDesc(UUID userId);

    long countByUserId(UUID userId);
}
