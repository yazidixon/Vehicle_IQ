package com.vehicleplatform.controller;

import com.vehicleplatform.dto.dashboard.DashboardResponse;
import com.vehicleplatform.entity.User;
import com.vehicleplatform.service.DashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponse dashboard(@AuthenticationPrincipal User currentUser) {
        return dashboardService.build(currentUser.getId());
    }
}
