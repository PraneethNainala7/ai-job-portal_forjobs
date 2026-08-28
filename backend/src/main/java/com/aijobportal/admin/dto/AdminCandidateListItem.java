package com.aijobportal.admin.dto;

public record AdminCandidateListItem(
        String id,
        String name,
        String email,
        String role,
        String accountStatus,
        String createdAt,
        String holdReason,
        String title,
        String location,
        String experience
) {
}
