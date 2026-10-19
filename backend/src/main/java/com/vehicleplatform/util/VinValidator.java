package com.vehicleplatform.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Structural VIN validation (ISO 3779): 17 alphanumeric chars, no I/O/Q. */
public final class VinValidator {

    private static final Pattern ALPHANUMERIC = Pattern.compile("^[A-Z0-9]*$");

    private VinValidator() {
    }

    public record ValidationResult(boolean valid, List<String> errors) {
    }

    public static ValidationResult isValidFormat(String rawVin) {
        List<String> errors = new ArrayList<>();
        String vin = rawVin == null ? "" : rawVin.strip().toUpperCase();

        if (vin.length() != 17) {
            errors.add("Le VIN doit contenir exactement 17 caractères (reçu : " + vin.length() + ").");
        }
        if (vin.chars().anyMatch(c -> c == 'I' || c == 'O' || c == 'Q')) {
            errors.add("Le VIN ne peut pas contenir les lettres I, O ou Q.");
        }
        if (!ALPHANUMERIC.matcher(vin).matches()) {
            errors.add("Le VIN ne doit contenir que des lettres et des chiffres (alphanumérique).");
        }
        return new ValidationResult(errors.isEmpty(), errors);
    }
}
