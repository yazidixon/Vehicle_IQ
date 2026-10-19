package com.vehicleplatform.repository;

import com.vehicleplatform.entity.ApiSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiSourceRepository extends JpaRepository<ApiSource, java.util.UUID> {
    Optional<ApiSource> findByName(String name);
}
