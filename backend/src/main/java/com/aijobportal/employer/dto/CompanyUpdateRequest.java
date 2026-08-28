package com.aijobportal.employer.dto;

public record CompanyUpdateRequest(
        String companyName,
        String companyInformation,
        String companyLocation,
        String companyWebsite
) {
}
