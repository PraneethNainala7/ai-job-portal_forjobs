package com.aijobportal.ai.dto;

import java.util.List;

public record MatchResultResponse(
        String jobId,
        String candidateId,
        int matchScore,
        List<String> strongAreas,
        List<String> gaps,
        String explanation
) {
}
