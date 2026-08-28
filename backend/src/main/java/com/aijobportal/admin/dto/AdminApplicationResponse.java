package com.aijobportal.admin.dto;

public record AdminApplicationResponse(
        String id,
        String jobRole,
        String companyName,
        String employerName,
        String candidateName,
        String status,
        String appliedAt
) {
}
