package com.vehicleplatform.dto.dashboard;

import java.util.List;

public record DashboardResponse(
        DashboardStats stats,
        List<DashboardDistributionItem> searchesByFuelType,
        List<DashboardDistributionItem> searchesOverTime,
        List<DashboardDistributionItem> averageHorsepowerByMake,
        List<DashboardDistributionItem> averageCo2ByMake
) {
}
