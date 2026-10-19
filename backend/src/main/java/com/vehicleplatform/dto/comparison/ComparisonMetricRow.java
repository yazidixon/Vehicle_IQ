package com.vehicleplatform.dto.comparison;

import java.util.Map;

public record ComparisonMetricRow(
        String key,
        String label,
        String unit,
        String betterDirection,
        Map<String, Object> values,
        String bestVehicleId
) {
}
