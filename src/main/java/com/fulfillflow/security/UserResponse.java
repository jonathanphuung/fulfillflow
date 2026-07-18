package com.fulfillflow.security;

import java.util.UUID;

public record UserResponse(UUID id, String email, String role, boolean active) {
    static UserResponse from(AppUser user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getRole().name(), user.isActive());
    }
}
