package com.aijobportal.job.dto;

import java.util.List;

public record JobListResponse(
        List<JobResponse> items,
        long total,
        int page,
        int pageSize
) {
}
