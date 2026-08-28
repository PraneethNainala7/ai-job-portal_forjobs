package com.aijobportal.ai.dto;

import com.aijobportal.job.dto.JobResponse;

public record RecommendationItem(JobResponse job, MatchResultResponse match) {
}
