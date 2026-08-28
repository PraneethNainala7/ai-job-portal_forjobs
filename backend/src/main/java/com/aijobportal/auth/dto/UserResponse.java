package com.aijobportal.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
        String id,
        String name,
        String email,
        String role,
        String accountStatus,
        String companyName,
        String companyInformation,
        String companyLocation,
        String cin,
        String companyWebsite,
        String rejectionReason,
        String createdAt,
        String holdReason
) {
}
