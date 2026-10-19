package com.vehicleplatform.dto.auth;

public record TokenResponse(
        String accessToken,
        String tokenType,
        UserResponse user
) {
    public static TokenResponse of(String token, UserResponse user) {
        return new TokenResponse(token, "bearer", user);
    }
}
