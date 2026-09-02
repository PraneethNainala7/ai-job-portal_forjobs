package com.aijobportal.ai.client;

import com.aijobportal.candidate.dto.ResumeAnalysisResponse;

import java.util.Map;

public record ResumeAnalysisOutcome(
        ResumeAnalysisResponse response,
        Map<String, Object> parsedData
) {
}
