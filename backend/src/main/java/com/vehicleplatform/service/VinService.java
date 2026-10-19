package com.vehicleplatform.service;

import com.vehicleplatform.adapter.NormalizedVinDecode;
import com.vehicleplatform.adapter.nhtsa.NhtsaProvider;
import com.vehicleplatform.entity.User;
import com.vehicleplatform.entity.VinDecode;
import com.vehicleplatform.exception.ApiException;
import com.vehicleplatform.repository.VinDecodeRepository;
import com.vehicleplatform.util.JsonUtil;
import com.vehicleplatform.util.VinValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class VinService {

    public static final String NOT_AVAILABLE = "Data not available";

    private final NhtsaProvider nhtsaProvider;
    private final CacheService cacheService;
    private final VinDecodeRepository vinDecodeRepository;
    private final JsonUtil jsonUtil;

    public VinService(NhtsaProvider nhtsaProvider, CacheService cacheService, VinDecodeRepository vinDecodeRepository, JsonUtil jsonUtil) {
        this.nhtsaProvider = nhtsaProvider;
        this.cacheService = cacheService;
        this.vinDecodeRepository = vinDecodeRepository;
        this.jsonUtil = jsonUtil;
    }

    public record DecodeOutcome(VinDecode record, boolean valid, List<String> warnings) {
    }

    @Transactional
    public DecodeOutcome decode(String rawVin, User user) {
        String vin = rawVin.strip().toUpperCase();
        var validation = VinValidator.isValidFormat(vin);
        if (!validation.valid()) {
            throw ApiException.unprocessable(String.join("; ", validation.errors()));
        }
        if (!nhtsaProvider.isConfigured()) {
            throw ApiException.serviceUnavailable("Le service de décodage VIN n'est pas configuré.");
        }

        // Cache the RAW upstream payload; normalization always runs fresh on top of it.
        var outcome = cacheService.getOrFetch(
                nhtsaProvider.getName(),
                "DecodeVinValuesExtended",
                Map.of("vin", vin),
                () -> nhtsaProvider.fetchRawVin(vin)
        );

        NormalizedVinDecode decoded = nhtsaProvider.normalize(vin, outcome.payload());

        VinDecode record = VinDecode.builder()
                .user(user)
                .vin(vin)
                .countryOfManufacture(decoded.getCountryOfManufacture())
                .manufacturer(decoded.getManufacturer())
                .plant(decoded.getPlant())
                .modelYear(decoded.getModelYear())
                .make(decoded.getMake())
                .model(decoded.getModel())
                .series(decoded.getSeries())
                .trim(decoded.getTrim())
                .engineCode(decoded.getEngineCode())
                .bodyClass(decoded.getBodyClass())
                .rawPayload(jsonUtil.toJson(decoded.getRawPayload()))
                .sourceProvider(decoded.getSourceProvider())
                .demoData(decoded.isDemoData())
                .build();
        record = vinDecodeRepository.save(record);

        return new DecodeOutcome(record, decoded.isValid(), decoded.getWarnings());
    }

    public List<VinDecode> historyForUser(UUID userId) {
        return vinDecodeRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
