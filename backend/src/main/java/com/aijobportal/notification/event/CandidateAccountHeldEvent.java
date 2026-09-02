package com.aijobportal.notification.event;

public record CandidateAccountHeldEvent(String email, String userName, String reason) {
}
