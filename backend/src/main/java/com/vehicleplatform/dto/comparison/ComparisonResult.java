package com.vehicleplatform.dto.comparison;

import com.vehicleplatform.dto.vehicle.VehicleResponse;

import java.util.List;

public record ComparisonResult(
        List<VehicleResponse> vehicles,
        List<ComparisonMetricRow> metrics
) {
}
