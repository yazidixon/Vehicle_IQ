package com.vehicleplatform.repository;

import com.vehicleplatform.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, UUID> {
    List<SearchHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);

    long countByUserIdAndSearchType(UUID userId, String searchType);
}
