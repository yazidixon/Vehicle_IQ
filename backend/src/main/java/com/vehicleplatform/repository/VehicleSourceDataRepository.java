package com.vehicleplatform.repository;

import com.vehicleplatform.entity.VehicleSourceData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VehicleSourceDataRepository extends JpaRepository<VehicleSourceData, UUID> {
    List<VehicleSourceData> findByVehicleId(UUID vehicleId);
}
