package com.vehicleplatform.repository;

import com.vehicleplatform.entity.Vehicle;
import org.springframework.data.jpa.domain.Specification;

public final class VehicleSpecifications {

    private VehicleSpecifications() {
    }

    public static Specification<Vehicle> makeEquals(String make) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("make")), make.toLowerCase());
    }

    public static Specification<Vehicle> modelContains(String model) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("model")), "%" + model.toLowerCase() + "%");
    }

    public static Specification<Vehicle> manufacturerEquals(String manufacturer) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("manufacturer")), manufacturer.toLowerCase());
    }

    public static Specification<Vehicle> yearEquals(Integer year) {
        return (root, query, cb) -> cb.equal(root.get("year"), year);
    }

    public static Specification<Vehicle> yearGte(Integer yearMin) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("year"), yearMin);
    }

    public static Specification<Vehicle> yearLte(Integer yearMax) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("year"), yearMax);
    }

    public static Specification<Vehicle> fuelTypeContains(String fuelType) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("fuelType")), "%" + fuelType.toLowerCase() + "%");
    }

    public static Specification<Vehicle> engineCodeContains(String engineCode) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("engineCode")), "%" + engineCode.toLowerCase() + "%");
    }

    /**
     * "Tesla Model 3" must match make="Tesla" + model="Model 3" even though no
     * single column contains the full phrase: each whitespace-separated token
     * has to appear in AT LEAST ONE of make/model/manufacturer/trim (AND of
     * per-token ORs), rather than requiring the whole phrase in one column.
     */
    public static Specification<Vehicle> freeText(String q) {
        return (root, query, cb) -> {
            String[] tokens = q.toLowerCase().trim().split("\\s+");
            var tokenPredicates = new jakarta.persistence.criteria.Predicate[tokens.length];
            for (int i = 0; i < tokens.length; i++) {
                String needle = "%" + tokens[i] + "%";
                tokenPredicates[i] = cb.or(
                        cb.like(cb.lower(root.get("make")), needle),
                        cb.like(cb.lower(root.get("model")), needle),
                        cb.like(cb.lower(root.get("manufacturer")), needle),
                        cb.like(cb.lower(cb.coalesce(root.get("trim"), "")), needle)
                );
            }
            return cb.and(tokenPredicates);
        };
    }
}
