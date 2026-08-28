package com.aijobportal.employer.dto;

public record EmployerDashboardResponse(
        int jobs,
        int activeJobs,
        int applicants,
        int shortlisted,
        int interviews
) {
}
