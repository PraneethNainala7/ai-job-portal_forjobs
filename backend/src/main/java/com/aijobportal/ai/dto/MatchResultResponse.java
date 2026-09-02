package com.aijobportal.ai.dto;

import java.util.List;

public record MatchResultResponse(
        String jobId,
        String candidateId,
        int matchScore,
        List<String> strongAreas,
        List<String> gaps,
        String explanation,
        List<String> matchedSkills,
        List<String> partiallyRelevantSkills,
        List<String> missingRequiredSkills,
        List<String> missingPreferredSkills,
        MatchScoreBreakdownDto scoreBreakdown,
        List<SkillMatchResponse> matchedSkillDetails,
        List<SkillMatchResponse> partiallyMatchedSkills,
        List<String> missingCriticalSkills,
        List<String> matchedPreferredSkills,
        boolean scoreCapApplied,
        String scoreCapReason,
        ScoreBreakdownResponse scoreBreakdownV2,
        ScoreBreakdownDetailResponse scoreBreakdownDetail,
        Integer totalEarnedPoints,
        Integer applicableMaximumPoints,
        boolean scoreReliable,
        String scoreUnreliableReason
) {
    public MatchResultResponse(
            String jobId,
            String candidateId,
            int matchScore,
            List<String> strongAreas,
            List<String> gaps,
            String explanation
    ) {
        this(
                jobId,
                candidateId,
                matchScore,
                strongAreas,
                gaps,
                explanation,
                strongAreas,
                List.of(),
                gaps,
                List.of(),
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                false,
                null,
                null,
                null,
                null,
                null,
                true,
                null
        );
    }
}
