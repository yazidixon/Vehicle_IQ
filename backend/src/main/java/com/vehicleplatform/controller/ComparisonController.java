package com.vehicleplatform.controller;

import com.vehicleplatform.dto.comparison.ComparisonRequest;
import com.vehicleplatform.dto.comparison.ComparisonResult;
import com.vehicleplatform.dto.comparison.SavedComparisonCreateRequest;
import com.vehicleplatform.dto.comparison.SavedComparisonResponse;
import com.vehicleplatform.dto.comparison.SavedComparisonUpdateRequest;
import com.vehicleplatform.entity.User;
import com.vehicleplatform.service.ComparisonService;
import com.vehicleplatform.service.SavedComparisonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comparisons")
public class ComparisonController {

    private final ComparisonService comparisonService;
    private final SavedComparisonService savedComparisonService;

    public ComparisonController(ComparisonService comparisonService, SavedComparisonService savedComparisonService) {
        this.comparisonService = comparisonService;
        this.savedComparisonService = savedComparisonService;
    }

    @PostMapping("/compute")
    public ComparisonResult compute(@Valid @RequestBody ComparisonRequest request) {
        return comparisonService.compare(request.vehicleIds());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SavedComparisonResponse save(@Valid @RequestBody SavedComparisonCreateRequest request,
                                         @AuthenticationPrincipal User currentUser) {
        var comparison = savedComparisonService.save(currentUser, request.name(), request.vehicleIds());
        return SavedComparisonResponse.from(comparison);
    }

    @GetMapping
    public List<SavedComparisonResponse> list(@AuthenticationPrincipal User currentUser) {
        return savedComparisonService.listForUser(currentUser.getId()).stream().map(SavedComparisonResponse::from).toList();
    }

    @GetMapping("/{id}")
    public SavedComparisonResponse get(@PathVariable UUID id, @AuthenticationPrincipal User currentUser) {
        return SavedComparisonResponse.from(savedComparisonService.get(id, currentUser.getId()));
    }

    @PutMapping("/{id}")
    public SavedComparisonResponse rename(@PathVariable UUID id, @Valid @RequestBody SavedComparisonUpdateRequest request,
                                           @AuthenticationPrincipal User currentUser) {
        return SavedComparisonResponse.from(savedComparisonService.rename(id, currentUser.getId(), request.name()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, @AuthenticationPrincipal User currentUser) {
        savedComparisonService.delete(id, currentUser.getId());
    }
}
