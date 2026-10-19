package com.vehicleplatform.controller;

import com.vehicleplatform.dto.auth.LoginRequest;
import com.vehicleplatform.dto.auth.RegisterRequest;
import com.vehicleplatform.dto.auth.TokenResponse;
import com.vehicleplatform.dto.auth.UpdateProfileRequest;
import com.vehicleplatform.dto.auth.UserResponse;
import com.vehicleplatform.entity.User;
import com.vehicleplatform.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request.email(), request.fullName(), request.password());
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.email(), request.password());
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal User currentUser) {
        return UserResponse.from(currentUser);
    }

    @PutMapping("/me")
    public UserResponse updateMe(@Valid @RequestBody UpdateProfileRequest request, @AuthenticationPrincipal User currentUser) {
        return authService.updateProfile(currentUser, request.fullName(), request.password());
    }
}
