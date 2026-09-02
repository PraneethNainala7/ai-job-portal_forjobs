package com.aijobportal.ai.dto;

import com.aijobportal.ai.matching.scoring.DimensionStatus;
import com.aijobportal.ai.matching.scoring.ScoringDimensionScore;

public record DimensionScoreResponse(
        Integer earnedPoints,
        int maxPoints,
        String status
) {
    public static DimensionScoreResponse from(ScoringDimensionScore score) {
        if (score == null) {
            return null;
        }
        return new DimensionScoreResponse(
                score.earnedPoints(),
                score.maxPoints(),
                score.status().name()
        );
    }
}
