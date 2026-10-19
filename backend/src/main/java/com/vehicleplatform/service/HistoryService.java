package com.vehicleplatform.service;

import com.vehicleplatform.entity.SearchHistory;
import com.vehicleplatform.entity.User;
import com.vehicleplatform.repository.SearchHistoryRepository;
import com.vehicleplatform.util.JsonUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class HistoryService {

    private final SearchHistoryRepository repository;
    private final JsonUtil jsonUtil;

    public HistoryService(SearchHistoryRepository repository, JsonUtil jsonUtil) {
        this.repository = repository;
        this.jsonUtil = jsonUtil;
    }

    public SearchHistory record(User user, String searchType, Map<String, Object> queryParams, String resultSummary) {
        SearchHistory entry = SearchHistory.builder()
                .user(user)
                .searchType(searchType)
                .queryParams(queryParams != null ? jsonUtil.toJson(queryParams) : null)
                .resultSummary(resultSummary)
                .build();
        return repository.save(entry);
    }

    public List<SearchHistory> listForUser(UUID userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Map<String, Object> parseParams(SearchHistory entry) {
        return entry.getQueryParams() != null ? jsonUtil.toMap(entry.getQueryParams()) : Map.of();
    }
}
