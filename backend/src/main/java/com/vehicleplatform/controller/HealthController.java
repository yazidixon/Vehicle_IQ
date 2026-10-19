package com.vehicleplatform.controller;

import com.vehicleplatform.config.AppProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    private final AppProperties appProperties;

    public HealthController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @GetMapping("/")
    public Map<String, Object> root() {
        return Map.of(
                "service", "Vehicle Intelligence Platform API",
                "version", "1.0.0",
                "demo_mode", appProperties.demoMode(),
                "docs", "/swagger-ui/index.html"
        );
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
