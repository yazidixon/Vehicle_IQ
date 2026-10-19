package com.vehicleplatform.dto.dashboard;

public record DashboardStats(
        long totalSearches,
        long totalVinDecodes,
        long totalVehiclesViewed,
        long totalComparisonsSaved
) {
}
