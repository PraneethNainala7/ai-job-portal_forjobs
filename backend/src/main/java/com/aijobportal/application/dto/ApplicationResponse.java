package com.aijobportal.application.dto;

import com.aijobportal.job.dto.JobResponse;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApplicationResponse(
        String id,
        String jobId,
        String candidateId,
        String status,
        String appliedAt,
        String rejectionReason,
        JobResponse job
) {
}
