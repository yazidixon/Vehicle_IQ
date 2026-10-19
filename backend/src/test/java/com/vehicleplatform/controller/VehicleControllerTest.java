package com.vehicleplatform.controller;

import com.vehicleplatform.dto.common.PaginatedResponse;
import com.vehicleplatform.dto.vehicle.VehicleResponse;
import com.vehicleplatform.entity.Vehicle;
import com.vehicleplatform.entity.VehicleSpecification;
import com.vehicleplatform.repository.VehicleRepository;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
class VehicleControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private VehicleRepository vehicleRepository;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private Vehicle seedVehicle(String make, String model, int year, int horsepower) {
        Vehicle vehicle = Vehicle.builder()
                .manufacturer(make).make(make).model(model).year(year)
                .fuelType("Diesel").sourceProvider("demo").demoData(true).build();
        vehicle = vehicleRepository.save(vehicle);
        VehicleSpecification spec = VehicleSpecification.builder()
                .vehicle(vehicle).horsepowerHp(horsepower).demoData(true).build();
        vehicle.setSpecification(spec);
        return vehicleRepository.save(vehicle);
    }

    @Test
void searchFiltersByMakeCaseInsensitively() {
        seedVehicle("BMW", "Serie 3", 2022, 190);

        ResponseEntity<PaginatedResponse> response = restTemplate.getForEntity(
                url("/api/vehicles?make={make}"), PaginatedResponse.class, "bmw");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().total()).isGreaterThanOrEqualTo(1);
    }

    @Test
void freeTextSearchMatchesAcrossMakeAndModelTokens() {
        seedVehicle("Tesla", "Model 3", 2024, 283);

        ResponseEntity<PaginatedResponse> response = restTemplate.getForEntity(
                url("/api/vehicles?q={q}"), PaginatedResponse.class, "Tesla Model 3");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().total()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void searchWithNoMatchesReturnsEmptyList() {
        ResponseEntity<PaginatedResponse> response = restTemplate.getForEntity(
                url("/api/vehicles?make={make}"), PaginatedResponse.class, "Bugatti-Nonexistent-XYZ");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().total()).isEqualTo(0);
        assertThat(response.getBody().items()).isEmpty();
    }

    @Test
void getVehicleByIdReturnsFullSpecification() {
        Vehicle vehicle = seedVehicle("Audi", "A4", 2022, 204);

        ResponseEntity<VehicleResponse> response = restTemplate.getForEntity(url("/api/vehicles/" + vehicle.getId()), VehicleResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().specification().horsepowerHp()).isEqualTo(204);
    }

    @Test
    void getUnknownVehicleReturns404() {
        ResponseEntity<Map> response = restTemplate.getForEntity(url("/api/vehicles/" + UUID.randomUUID()), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
void vehicleSourcesEndpointReturnsList() {
        Vehicle vehicle = seedVehicle("Peugeot", "308", 2023, 130);

        ResponseEntity<Object[]> response = restTemplate.getForEntity(url("/api/vehicles/" + vehicle.getId() + "/sources"), Object[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
