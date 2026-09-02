package com.aijobportal.notification.event;

public record CandidateShortlistedEvent(
        String candidateEmail,
        String candidateName,
        String jobTitle,
        String companyName
) {
}
