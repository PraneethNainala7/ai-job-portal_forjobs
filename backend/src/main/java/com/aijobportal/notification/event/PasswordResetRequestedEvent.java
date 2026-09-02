package com.aijobportal.notification.event;

public record PasswordResetRequestedEvent(String email, String userName, String resetLink) {
}
