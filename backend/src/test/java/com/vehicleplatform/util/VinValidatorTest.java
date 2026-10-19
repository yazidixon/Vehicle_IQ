package com.vehicleplatform.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class VinValidatorTest {

    @ParameterizedTest
    @CsvSource({
            "1HGCM82633A004352, true",   // 17 chars, valid alphabet
            "1HGCM82633A00435, false",   // 16 chars: too short
            "1HGCM82633A0043522, false", // 18 chars: too long
            "1HGCM8263IA004352, false",  // contains I
            "1HGCM8263OA004352, false",  // contains O
            "1HGCM8263QA004352, false",  // contains Q
    })
    void validatesVinFormat(String vin, boolean expectedValid) {
        var result = VinValidator.isValidFormat(vin);
        assertThat(result.valid()).isEqualTo(expectedValid);
        if (!expectedValid) {
            assertThat(result.errors()).isNotEmpty();
        }
    }

    @org.junit.jupiter.api.Test
    void rejectsNonAlphanumericCharacters() {
        var result = VinValidator.isValidFormat("1HG-M82633A004352");
        assertThat(result.valid()).isFalse();
    }

    @org.junit.jupiter.api.Test
    void trimsAndUppercasesBeforeValidating() {
        var result = VinValidator.isValidFormat("  1hgcm82633a004352  ");
        assertThat(result.valid()).isTrue();
    }
}
