package com.aijobportal.ai.dto;

import com.aijobportal.ai.matching.scoring.ScoredBreakdown;

public record ScoreBreakdownDetailResponse(
        DimensionScoreResponse requiredSkills,
        DimensionScoreResponse preferredSkills,
        DimensionScoreResponse experience,
        DimensionScoreResponse roleRelevance,
        DimensionScoreResponse education,
        int totalEarnedPoints,
        int applicableMaximumPoints,
        String finalCalculation
) {
    public static ScoreBreakdownDetailResponse from(
            ScoredBreakdown breakdown,
            int totalEarnedPoints,
            int applicableMaximumPoints
    ) {
        if (breakdown == null) {
            return null;
        }
        String calculation = applicableMaximumPoints > 0
                ? "(" + totalEarnedPoints + " / " + applicableMaximumPoints + ") × 100"
                : null;
        return new ScoreBreakdownDetailResponse(
                DimensionScoreResponse.from(breakdown.requiredSkills()),
                DimensionScoreResponse.from(breakdown.preferredSkills()),
                DimensionScoreResponse.from(breakdown.experience()),
                DimensionScoreResponse.from(breakdown.roleRelevance()),
                DimensionScoreResponse.from(breakdown.education()),
                totalEarnedPoints,
                applicableMaximumPoints,
                calculation
        );
    }
}
