package com.vehicleplatform.controller;

import com.vehicleplatform.dto.common.PaginatedResponse;
import com.vehicleplatform.dto.vehicle.VehicleResponse;
import com.vehicleplatform.dto.vehicle.VehicleSearchFilters;
import com.vehicleplatform.dto.vehicle.VehicleSourceResponse;
import com.vehicleplatform.dto.vehicle.VehicleSummaryResponse;
import com.vehicleplatform.entity.User;
import com.vehicleplatform.entity.Vehicle;
import com.vehicleplatform.exception.ApiException;
import com.vehicleplatform.service.HistoryService;
import com.vehicleplatform.service.VehicleImageService;
import com.vehicleplatform.service.VehicleService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
    private final HistoryService historyService;
    private final VehicleImageService vehicleImageService;

    public VehicleController(VehicleService vehicleService, HistoryService historyService, VehicleImageService vehicleImageService) {
        this.vehicleService = vehicleService;
        this.historyService = historyService;
        this.vehicleImageService = vehicleImageService;
    }

    private PaginatedResponse<VehicleSummaryResponse> runSearch(
            String q, String make, String model, String manufacturer, Integer year, Integer yearMin, Integer yearMax,
            String fuelType, String engineCode, int page, int pageSize, User currentUser
    ) {
        var filters = new VehicleSearchFilters(q, make, model, manufacturer, year, yearMin, yearMax, fuelType, engineCode, page, pageSize);
        Page<Vehicle> result = vehicleService.search(filters);

        if (currentUser != null) {
            Map<String, Object> params = new LinkedHashMap<>();
            if (q != null) params.put("q", q);
            if (make != null) params.put("make", make);
            if (model != null) params.put("model", model);
            if (manufacturer != null) params.put("manufacturer", manufacturer);
            if (year != null) params.put("year", year);
            if (yearMin != null) params.put("year_min", yearMin);
            if (yearMax != null) params.put("year_max", yearMax);
            if (fuelType != null) params.put("fuel_type", fuelType);
            if (engineCode != null) params.put("engine_code", engineCode);
            historyService.record(currentUser, "filter", params, result.getTotalElements() + " véhicule(s) trouvé(s)");
        }

        result.getContent().forEach(v -> vehicleImageService.ensureImage(v).ifPresent(v::setImageUrl));

        return new PaginatedResponse<>(
                result.getContent().stream().map(VehicleSummaryResponse::from).toList(),
                result.getTotalElements(), page, pageSize, Math.max(result.getTotalPages(), 1)
        );
    }

    @GetMapping
    public PaginatedResponse<VehicleSummaryResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String make,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) Integer year,
            @RequestParam(name = "year_min", required = false) Integer yearMin,
            @RequestParam(name = "year_max", required = false) Integer yearMax,
            @RequestParam(name = "fuel_type", required = false) String fuelType,
            @RequestParam(name = "engine_code", required = false) String engineCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "page_size", defaultValue = "12") int pageSize,
            @AuthenticationPrincipal User currentUser
    ) {
        return runSearch(q, make, model, manufacturer, year, yearMin, yearMax, fuelType, engineCode, page, pageSize, currentUser);
    }

    @GetMapping("/search")
    public PaginatedResponse<VehicleSummaryResponse> searchAlias(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String make,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) Integer year,
            @RequestParam(name = "year_min", required = false) Integer yearMin,
            @RequestParam(name = "year_max", required = false) Integer yearMax,
            @RequestParam(name = "fuel_type", required = false) String fuelType,
            @RequestParam(name = "engine_code", required = false) String engineCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "page_size", defaultValue = "12") int pageSize,
            @AuthenticationPrincipal User currentUser
    ) {
        return runSearch(q, make, model, manufacturer, year, yearMin, yearMax, fuelType, engineCode, page, pageSize, currentUser);
    }

    @GetMapping("/meta/makes")
    public List<String> makes() {
        return vehicleService.listMakes();
    }

    @GetMapping("/meta/models")
    public List<String> models(@RequestParam String make) {
        return vehicleService.listModels(make);
    }

    @GetMapping("/{id}")
    public VehicleResponse getById(@PathVariable UUID id, @AuthenticationPrincipal User currentUser) {
        Vehicle vehicle = vehicleService.getById(id).orElseThrow(() -> ApiException.notFound("Véhicule introuvable."));
        vehicleImageService.ensureImage(vehicle).ifPresent(vehicle::setImageUrl);
        if (currentUser != null) {
            historyService.record(currentUser, "vehicle_view", Map.of("vehicle_id", id.toString()),
                    vehicle.getMake() + " " + vehicle.getModel() + " (" + vehicle.getYear() + ")");
        }
        return VehicleResponse.from(vehicle);
    }

    @GetMapping("/{id}/sources")
    public List<VehicleSourceResponse> sources(@PathVariable UUID id) {
        vehicleService.getById(id).orElseThrow(() -> ApiException.notFound("Véhicule introuvable."));
        return vehicleService.getSources(id).stream().map(VehicleSourceResponse::from).toList();
    }
}
