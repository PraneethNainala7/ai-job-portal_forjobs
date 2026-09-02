package com.aijobportal.notification.event;

public record ApplicationSubmittedEvent(
        String candidateEmail,
        String candidateName,
        String jobTitle,
        String companyName
) {
}
