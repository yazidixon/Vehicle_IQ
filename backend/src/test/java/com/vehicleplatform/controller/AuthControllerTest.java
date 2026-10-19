package com.vehicleplatform.controller;

import com.vehicleplatform.dto.auth.LoginRequest;
import com.vehicleplatform.dto.auth.RegisterRequest;
import com.vehicleplatform.dto.auth.TokenResponse;
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
class AuthControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private String uniqueEmail() {
        return "user_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    @Test
    void registerCreatesUserAndReturnsToken() {
        var request = new RegisterRequest(uniqueEmail(), "Alice", "StrongPass1");

        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(url("/api/auth/register"), request, TokenResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isNotBlank();
        assertThat(response.getBody().user().email()).isEqualTo(request.email());
        assertThat(response.getBody().user().createdAt()).isNotNull();
    }

    @Test
    void registerRejectsDuplicateEmail() {
        var email = uniqueEmail();
        var request = new RegisterRequest(email, "Bob", "StrongPass1");
        restTemplate.postForEntity(url("/api/auth/register"), request, TokenResponse.class);

        ResponseEntity<Map> second = restTemplate.postForEntity(url("/api/auth/register"), request, Map.class);

        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void loginWithCorrectCredentialsSucceeds() {
        var email = uniqueEmail();
        restTemplate.postForEntity(url("/api/auth/register"), new RegisterRequest(email, "Carol", "StrongPass1"), TokenResponse.class);

        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
                url("/api/auth/login"), new LoginRequest(email, "StrongPass1"), TokenResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().user().email()).isEqualTo(email);
    }

    @Test
    void loginWithWrongPasswordIsRejected() {
        var email = uniqueEmail();
        restTemplate.postForEntity(url("/api/auth/register"), new RegisterRequest(email, "Dave", "StrongPass1"), TokenResponse.class);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/auth/login"), new LoginRequest(email, "WrongPassword"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void meRequiresAuthentication() {
        ResponseEntity<Map> response = restTemplate.getForEntity(url("/api/auth/me"), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void meReturnsCurrentUserProfileWhenAuthenticated() {
        var email = uniqueEmail();
        var registerResponse = restTemplate.postForEntity(
                url("/api/auth/register"), new RegisterRequest(email, "Erin", "StrongPass1"), TokenResponse.class);
        String token = registerResponse.getBody().accessToken();

        var headers = new org.springframework.http.HttpHeaders();
        headers.setBearerAuth(token);
        var entity = new org.springframework.http.HttpEntity<Void>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url("/api/auth/me"), org.springframework.http.HttpMethod.GET, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("email")).isEqualTo(email);
    }
}
