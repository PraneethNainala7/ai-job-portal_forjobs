package com.aijobportal.admin.dto;

import com.aijobportal.audit.entity.AuditLog;
import com.aijobportal.job.dto.JobResponse;

import java.util.List;
import java.util.Map;

public record AdminOverviewResponse(
        AdminStatsResponse stats,
        List<JobResponse> recentJobs,
        List<AdminApplicationResponse> recentApplications,
        List<AuditLog> recentAudit,
        Map<String, Integer> aiUsage
) {
}
