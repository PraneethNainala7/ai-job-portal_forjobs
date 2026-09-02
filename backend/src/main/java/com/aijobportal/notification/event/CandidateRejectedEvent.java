package com.aijobportal.notification.event;

public record CandidateRejectedEvent(
        String candidateEmail,
        String candidateName,
        String jobTitle,
        String companyName
) {
}
