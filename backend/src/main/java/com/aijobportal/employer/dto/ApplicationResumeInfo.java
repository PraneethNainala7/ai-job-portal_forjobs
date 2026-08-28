package com.aijobportal.employer.dto;

public record ApplicationResumeInfo(
        String fileName,
        String appliedAt,
        boolean downloadable
) {
}
