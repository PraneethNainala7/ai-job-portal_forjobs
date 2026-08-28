package com.aijobportal.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Enter a valid email and password.")
        @Email(message = "Enter a valid email and password.")
        String email,
        @NotBlank(message = "Enter a valid email and password.")
        String password
) {
}
