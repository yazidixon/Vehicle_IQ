package com.vehicleplatform.dto.history;

import com.vehicleplatform.entity.SearchHistory;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record SearchHistoryResponse(
        UUID id,
        String searchType,
        Map<String, Object> queryParams,
        String resultSummary,
        Instant createdAt
) {
    public static SearchHistoryResponse from(SearchHistory h, Map<String, Object> parsedParams) {
        return new SearchHistoryResponse(h.getId(), h.getSearchType(), parsedParams, h.getResultSummary(), h.getCreatedAt());
    }
}
