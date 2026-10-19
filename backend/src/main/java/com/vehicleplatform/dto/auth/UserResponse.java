package com.vehicleplatform.dto.auth;

import com.vehicleplatform.entity.User;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String fullName,
        boolean isActive,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.isActive(), user.getCreatedAt());
    }
}
