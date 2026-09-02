package com.aijobportal.employer.dto;

public record EmployerOnboardingRequest(
        String companyName,
        String companyInformation,
        String companyLocation,
        String companyWebsite,
        String cin
) {
}
