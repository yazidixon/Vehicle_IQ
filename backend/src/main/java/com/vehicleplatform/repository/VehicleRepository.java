package com.vehicleplatform.repository;

import com.vehicleplatform.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID>, JpaSpecificationExecutor<Vehicle> {

    // Existence check only (both call sites only ever tested .isPresent()) —
    // deliberately NOT a findBy...-returning-Optional<Vehicle>: some real
    // make/model/year combinations legitimately have more than one row
    // (different trims/engines of the same model year), which made the old
    // findBy version throw NonUniqueResultException instead of just answering
    // "yes, it already exists". exists queries tolerate duplicates fine.
    boolean existsByMakeIgnoreCaseAndModelIgnoreCaseAndYear(String make, String model, Integer year);

    @Query("select distinct v.make from Vehicle v order by v.make")
    List<String> findDistinctMakes();

    @Query("select distinct v.model from Vehicle v where lower(v.make) = lower(:make) order by v.model")
    List<String> findDistinctModelsByMake(String make);

    @Query("select v.make as make, avg(s.horsepowerHp) as value from Vehicle v join v.specification s " +
            "where s.horsepowerHp is not null group by v.make order by v.make")
    List<MakeAggregate> averageHorsepowerByMake();

    @Query("select v.make as make, avg(s.co2EmissionsGkm) as value from Vehicle v join v.specification s " +
            "where s.co2EmissionsGkm is not null group by v.make order by v.make")
    List<MakeAggregate> averageCo2ByMake();

    interface MakeAggregate {
        String getMake();
        Double getValue();
    }
}
