package com.aijobportal.notification.event;

public record EmployerAccountRestoredEvent(String email, String userName, String companyName) {
}
