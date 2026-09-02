package com.aijobportal.auth.dto;

public record GoogleAuthResponse(UserResponse user, boolean needsEmployerOnboarding) {
}
