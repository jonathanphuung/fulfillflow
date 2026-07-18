package com.fulfillflow.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 12, max = 72) String password,
        @NotBlank @Pattern(regexp = "ADMIN|WORKER") String role) {
}
