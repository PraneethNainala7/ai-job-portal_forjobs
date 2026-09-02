package com.aijobportal.ai.dto;

import com.aijobportal.ai.matching.scoring.ScoreBreakdown;

public record MatchScoreBreakdownDto(
        int requiredSkills,
        int preferredSkills,
        int experience,
        int roleRelevance,
        int semanticSimilarity,
        int education
) {
    public static MatchScoreBreakdownDto from(ScoreBreakdown breakdown) {
        if (breakdown == null) {
            return null;
        }
        return new MatchScoreBreakdownDto(
                breakdown.criticalRequiredSkills() + breakdown.requiredSkills(),
                breakdown.preferredSkills(),
                breakdown.relevantExperience(),
                breakdown.roleRelevance(),
                0,
                breakdown.educationAndCertifications()
        );
    }
}
