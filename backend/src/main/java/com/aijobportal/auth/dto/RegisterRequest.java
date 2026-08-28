package com.aijobportal.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Name is required.")
        @Size(min = 2, message = "Name is required.")
        String name,
        @NotBlank(message = "Enter a valid email and password.")
        @Email(message = "Enter a valid email and password.")
        String email,
        @NotBlank(message = "Password must be at least 8 characters.")
        @Size(min = 8, message = "Password must be at least 8 characters.")
        String password,
        @NotBlank(message = "Role is required.")
        String role,
        String companyName,
        String companyInformation,
        String companyLocation,
        String companyWebsite,
        String cin
) {
}
