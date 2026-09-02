package com.aijobportal.notification.event;

public record EmployerAccountHeldEvent(String email, String userName, String companyName, String reason) {
}
