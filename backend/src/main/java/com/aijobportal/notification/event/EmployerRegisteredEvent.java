package com.aijobportal.notification.event;

public record EmployerRegisteredEvent(String email, String userName, String companyName) {
}
