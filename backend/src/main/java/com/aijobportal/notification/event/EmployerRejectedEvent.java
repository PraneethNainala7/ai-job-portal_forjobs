package com.aijobportal.notification.event;

public record EmployerRejectedEvent(String email, String userName, String companyName, String reason) {
}
