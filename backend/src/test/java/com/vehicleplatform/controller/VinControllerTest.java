package com.vehicleplatform.controller;

import com.vehicleplatform.dto.vin.VinDecodeRequest;
import com.vehicleplatform.dto.vin.VinDecodeResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the VIN endpoint against the REAL NHTSA vPIC API (no mocking) —
 * this project's whole point is a genuine external integration, so the test
 * suite verifies that integration actually works, not a stand-in for it.
 * Requires internet access; the same free, key-less API the app calls at runtime.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
class VinControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void rejectsMalformedVin() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/vin/decode"), new VinDecodeRequest("TOO_SHORT_AND_INVALID"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @Test
    void rejectsVinWithForbiddenLetters() {
        // Contains 'I', 'O', 'Q' which ISO 3779 forbids
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/vin/decode"), new VinDecodeRequest("1HGCM8263IOQ04352"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @Test
    void decodesKnownVinViaRealNhtsaApiAndNeverInventsMissingFields() {
        ResponseEntity<VinDecodeResponse> response = restTemplate.postForEntity(
                url("/api/vin/decode"), new VinDecodeRequest("1HGCM82633A004352"), VinDecodeResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        VinDecodeResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.make()).isEqualToIgnoringCase("HONDA");
        assertThat(body.sourceProvider()).isEqualTo("nhtsa");
        assertThat(body.isDemoData()).isFalse();
        // Every field must be either a real value or the exact sentinel — never blank/null.
        assertThat(body.series()).isNotBlank();
    }
}
