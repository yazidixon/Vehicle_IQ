package com.vehicleplatform.controller;

import com.vehicleplatform.dto.auth.RegisterRequest;
import com.vehicleplatform.dto.auth.TokenResponse;
import com.vehicleplatform.dto.comparison.SavedComparisonCreateRequest;
import com.vehicleplatform.dto.comparison.SavedComparisonResponse;
import com.vehicleplatform.dto.comparison.SavedComparisonUpdateRequest;
import com.vehicleplatform.entity.Vehicle;
import com.vehicleplatform.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression coverage for a real bug found while manually testing the app:
 * SavedComparison.vehicleIds is a lazy @ElementCollection, and with
 * open-in-view disabled, serializing it after the transaction closes threw
 * LazyInitializationException on every listing/detail call. Fixed by making
 * the collection EAGER (see SavedComparison.java).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
class ComparisonControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private VehicleRepository vehicleRepository;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpHeaders authHeaders() {
        var email = "comp_user_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        var register = restTemplate.postForEntity(
                url("/api/auth/register"), new RegisterRequest(email, "Comp User", "StrongPass1"), TokenResponse.class);
        var headers = new HttpHeaders();
        headers.setBearerAuth(register.getBody().accessToken());
        return headers;
    }

    private UUID seedVehicleId(String make) {
        Vehicle vehicle = Vehicle.builder()
                .manufacturer(make).make(make).model("Model").year(2023)
                .sourceProvider("demo").demoData(true).build();
        return vehicleRepository.save(vehicle).getId();
    }

    @Test
    void savingAndThenListingComparisonsDoesNotThrowLazyInitializationException() {
        var headers = authHeaders();
        List<UUID> ids = List.of(seedVehicleId("A"), seedVehicleId("B"));

        var createRequest = new HttpEntity<>(new SavedComparisonCreateRequest("Mon comparatif", ids), headers);
        ResponseEntity<SavedComparisonResponse> created = restTemplate.postForEntity(url("/api/comparisons"), createRequest, SavedComparisonResponse.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody().vehicleIds()).containsExactlyElementsOf(ids);

        var listEntity = new HttpEntity<Void>(headers);
        ResponseEntity<SavedComparisonResponse[]> list = restTemplate.exchange(
                url("/api/comparisons"), HttpMethod.GET, listEntity, SavedComparisonResponse[].class);

        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(list.getBody()).isNotEmpty();
        assertThat(list.getBody()[0].vehicleIds()).isNotEmpty();
    }

    @Test
    void gettingASingleSavedComparisonReturnsItsVehicleIds() {
        var headers = authHeaders();
        List<UUID> ids = List.of(seedVehicleId("C"), seedVehicleId("D"));
        var createRequest = new HttpEntity<>(new SavedComparisonCreateRequest("Comparatif 2", ids), headers);
        var created = restTemplate.postForEntity(url("/api/comparisons"), createRequest, SavedComparisonResponse.class);

        var getEntity = new HttpEntity<Void>(headers);
        ResponseEntity<SavedComparisonResponse> response = restTemplate.exchange(
                url("/api/comparisons/" + created.getBody().id()), HttpMethod.GET, getEntity, SavedComparisonResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().vehicleIds()).containsExactlyElementsOf(ids);
    }

    @Test
    void renamingAndDeletingASavedComparisonWorks() {
        var headers = authHeaders();
        List<UUID> ids = List.of(seedVehicleId("E"), seedVehicleId("F"));
        var createRequest = new HttpEntity<>(new SavedComparisonCreateRequest("Ancien nom", ids), headers);
        var created = restTemplate.postForEntity(url("/api/comparisons"), createRequest, SavedComparisonResponse.class);
        UUID id = created.getBody().id();

        var renameEntity = new HttpEntity<>(new SavedComparisonUpdateRequest("Nouveau nom"), headers);
        ResponseEntity<SavedComparisonResponse> renamed = restTemplate.exchange(
                url("/api/comparisons/" + id), HttpMethod.PUT, renameEntity, SavedComparisonResponse.class);
        assertThat(renamed.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(renamed.getBody().name()).isEqualTo("Nouveau nom");

        var deleteEntity = new HttpEntity<Void>(headers);
        ResponseEntity<Void> deleted = restTemplate.exchange(
                url("/api/comparisons/" + id), HttpMethod.DELETE, deleteEntity, Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Object> afterDelete = restTemplate.exchange(
                url("/api/comparisons/" + id), HttpMethod.GET, deleteEntity, Object.class);
        assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
