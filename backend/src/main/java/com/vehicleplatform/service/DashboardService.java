package com.vehicleplatform.service;

import com.vehicleplatform.dto.dashboard.DashboardDistributionItem;
import com.vehicleplatform.dto.dashboard.DashboardResponse;
import com.vehicleplatform.dto.dashboard.DashboardStats;
import com.vehicleplatform.entity.SearchHistory;
import com.vehicleplatform.repository.SavedComparisonRepository;
import com.vehicleplatform.repository.SearchHistoryRepository;
import com.vehicleplatform.repository.VehicleRepository;
import com.vehicleplatform.repository.VinDecodeRepository;
import com.vehicleplatform.util.JsonUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Service
public class DashboardService {

    private final SearchHistoryRepository historyRepository;
    private final VinDecodeRepository vinDecodeRepository;
    private final SavedComparisonRepository savedComparisonRepository;
    private final VehicleRepository vehicleRepository;
    private final JsonUtil jsonUtil;

    public DashboardService(SearchHistoryRepository historyRepository, VinDecodeRepository vinDecodeRepository,
                             SavedComparisonRepository savedComparisonRepository, VehicleRepository vehicleRepository,
                             JsonUtil jsonUtil) {
        this.historyRepository = historyRepository;
        this.vinDecodeRepository = vinDecodeRepository;
        this.savedComparisonRepository = savedComparisonRepository;
        this.vehicleRepository = vehicleRepository;
        this.jsonUtil = jsonUtil;
    }

    @Transactional(readOnly = true)
    public DashboardResponse build(UUID userId) {
        List<SearchHistory> history = historyRepository.findByUserIdOrderByCreatedAtDesc(userId);

        long totalSearches = history.stream().filter(h -> h.getSearchType().equals("filter")).count();
        long totalVehiclesViewed = history.stream().filter(h -> h.getSearchType().equals("vehicle_view")).count();
        long totalVinDecodes = vinDecodeRepository.countByUserId(userId);
        long totalComparisonsSaved = savedComparisonRepository.countByUserId(userId);

        DashboardStats stats = new DashboardStats(totalSearches, totalVinDecodes, totalVehiclesViewed, totalComparisonsSaved);

        Map<String, Long> fuelCounter = new TreeMap<>();
        Map<String, Long> dayCounter = new TreeMap<>();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);

        for (SearchHistory h : history) {
            Map<String, Object> params = h.getQueryParams() != null ? jsonUtil.toMap(h.getQueryParams()) : Map.of();
            Object fuelType = params.get("fuel_type") != null ? params.get("fuel_type") : params.get("fuelType");
            if (fuelType != null) {
                fuelCounter.merge(fuelType.toString(), 1L, Long::sum);
            }
            String day = dayFormatter.format(h.getCreatedAt());
            dayCounter.merge(day, 1L, Long::sum);
        }

        List<DashboardDistributionItem> searchesByFuelType = fuelCounter.entrySet().stream()
                .map(e -> new DashboardDistributionItem(e.getKey(), e.getValue())).toList();
        List<DashboardDistributionItem> searchesOverTime = dayCounter.entrySet().stream()
                .map(e -> new DashboardDistributionItem(e.getKey(), e.getValue())).toList();

        List<DashboardDistributionItem> avgHp = vehicleRepository.averageHorsepowerByMake().stream()
                .map(a -> new DashboardDistributionItem(a.getMake(), Math.round(a.getValue() * 10.0) / 10.0)).toList();
        List<DashboardDistributionItem> avgCo2 = vehicleRepository.averageCo2ByMake().stream()
                .map(a -> new DashboardDistributionItem(a.getMake(), Math.round(a.getValue() * 10.0) / 10.0)).toList();

        return new DashboardResponse(stats, searchesByFuelType, searchesOverTime, avgHp, avgCo2);
    }
}
