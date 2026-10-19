package com.vehicleplatform.exception;

import com.vehicleplatform.adapter.ProviderException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException exc) {
        if (exc.getStatus().is5xxServerError()) {
            log.error("API error [{}]: {}", exc.getErrorCode(), exc.getMessage());
        } else {
            log.info("API error [{}]: {}", exc.getErrorCode(), exc.getMessage());
        }
        return ResponseEntity.status(exc.getStatus()).body(Map.of(
                "error", exc.getErrorCode(),
                "message", exc.getMessage()
        ));
    }

    @ExceptionHandler(ProviderException.class)
    public ResponseEntity<Map<String, Object>> handleProviderException(ProviderException exc) {
        log.error("Provider error [{}]: {}", exc.getProvider(), exc.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                "error", "external_api_unavailable",
                "message", "Le fournisseur de données '" + exc.getProvider() + "' est momentanément indisponible.",
                "provider", exc.getProvider()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException exc) {
        Map<String, String> details = new HashMap<>();
        exc.getBindingResult().getFieldErrors().forEach(err -> details.put(err.getField(), err.getDefaultMessage()));
        log.info("Validation error: {}", details);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(Map.of(
                "error", "validation_error",
                "message", "Les données envoyées sont invalides.",
                "details", details
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception exc) {
        log.error("Unhandled exception", exc);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "internal_error",
                "message", "Une erreur interne est survenue."
        ));
    }
}
