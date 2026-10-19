package com.vehicleplatform.service;

import com.vehicleplatform.entity.SavedComparison;
import com.vehicleplatform.entity.User;
import com.vehicleplatform.exception.ApiException;
import com.vehicleplatform.repository.SavedComparisonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SavedComparisonService {

    private final SavedComparisonRepository repository;

    public SavedComparisonService(SavedComparisonRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public SavedComparison save(User user, String name, List<UUID> vehicleIds) {
        SavedComparison comparison = SavedComparison.builder()
                .user(user).name(name).vehicleIds(new java.util.ArrayList<>(vehicleIds)).build();
        // saveAndFlush so @CreationTimestamp/@UpdateTimestamp are populated in time
        // for the response DTO built right after this call returns.
        return repository.saveAndFlush(comparison);
    }

    public List<SavedComparison> listForUser(UUID userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public SavedComparison get(UUID comparisonId, UUID userId) {
        return repository.findByIdAndUserId(comparisonId, userId)
                .orElseThrow(() -> ApiException.notFound("Comparaison introuvable."));
    }

    @Transactional
    public SavedComparison rename(UUID comparisonId, UUID userId, String name) {
        SavedComparison comparison = get(comparisonId, userId);
        comparison.setName(name);
        return repository.saveAndFlush(comparison);
    }

    @Transactional
    public void delete(UUID comparisonId, UUID userId) {
        repository.delete(get(comparisonId, userId));
    }
}
