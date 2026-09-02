package com.aijobportal.notification.event;

public record EmployerApprovedEvent(String email, String userName, String companyName) {
}
