package com.vehicleplatform.controller;

import com.vehicleplatform.dto.vin.VinDecodeRequest;
import com.vehicleplatform.dto.vin.VinDecodeResponse;
import com.vehicleplatform.entity.User;
import com.vehicleplatform.entity.VinDecode;
import com.vehicleplatform.service.HistoryService;
import com.vehicleplatform.service.VinService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.vehicleplatform.service.VinService.NOT_AVAILABLE;

@RestController
@RequestMapping("/api/vin")
public class VinController {

    private final VinService vinService;
    private final HistoryService historyService;

    public VinController(VinService vinService, HistoryService historyService) {
        this.vinService = vinService;
        this.historyService = historyService;
    }

    @PostMapping("/decode")
    public VinDecodeResponse decode(@Valid @RequestBody VinDecodeRequest request, @AuthenticationPrincipal User currentUser) {
        VinService.DecodeOutcome outcome = vinService.decode(request.vin(), currentUser);
        VinDecode record = outcome.record();

        if (currentUser != null) {
            String summary = (record.getMake() != null ? record.getMake() : NOT_AVAILABLE)
                    + " " + (record.getModel() != null ? record.getModel() : "");
            historyService.record(currentUser, "vin", Map.of("vin", request.vin()), summary.strip());
        }

        return new VinDecodeResponse(
                record.getVin(), outcome.valid(),
                orNotAvailable(record.getCountryOfManufacture()), orNotAvailable(record.getManufacturer()),
                orNotAvailable(record.getPlant()), orNotAvailable(record.getModelYear()),
                orNotAvailable(record.getMake()), orNotAvailable(record.getModel()),
                orNotAvailable(record.getSeries()), orNotAvailable(record.getTrim()),
                orNotAvailable(record.getEngineCode()), orNotAvailable(record.getBodyClass()),
                record.getSourceProvider(), record.isDemoData(), outcome.warnings()
        );
    }

    private String orNotAvailable(String value) {
        return (value == null || value.isBlank()) ? NOT_AVAILABLE : value;
    }
}
