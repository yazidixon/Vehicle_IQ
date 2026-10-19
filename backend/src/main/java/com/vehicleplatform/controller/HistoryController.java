package com.vehicleplatform.controller;

import com.vehicleplatform.dto.history.SearchHistoryResponse;
import com.vehicleplatform.entity.User;
import com.vehicleplatform.service.HistoryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/history")
    public List<SearchHistoryResponse> history(@AuthenticationPrincipal User currentUser) {
        return historyService.listForUser(currentUser.getId()).stream()
                .map(h -> SearchHistoryResponse.from(h, historyService.parseParams(h)))
                .toList();
    }
}
