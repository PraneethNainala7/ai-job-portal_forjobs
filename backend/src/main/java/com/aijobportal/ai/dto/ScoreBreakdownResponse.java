package com.aijobportal.ai.dto;

import com.aijobportal.ai.matching.scoring.ScoreBreakdown;

public record ScoreBreakdownResponse(
        int criticalRequiredSkills,
        int requiredSkills,
        int preferredSkills,
        int relevantExperience,
        int roleRelevance,
        int educationAndCertifications
) {
    public static ScoreBreakdownResponse from(ScoreBreakdown breakdown) {
        if (breakdown == null) {
            return null;
        }
        return new ScoreBreakdownResponse(
                breakdown.criticalRequiredSkills(),
                breakdown.requiredSkills(),
                breakdown.preferredSkills(),
                breakdown.relevantExperience(),
                breakdown.roleRelevance(),
                breakdown.educationAndCertifications()
        );
    }
}
