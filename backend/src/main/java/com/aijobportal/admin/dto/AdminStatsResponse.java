package com.aijobportal.admin.dto;

public record AdminStatsResponse(
        long candidates,
        long employers,
        long users,
        long jobs,
        long activeJobs,
        long closedJobs,
        long applications,
        long pendingEmployers
) {
}
