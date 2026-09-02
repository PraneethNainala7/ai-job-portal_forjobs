package com.aijobportal.ai.matching.scoring;

public record ScoringDimensionScore(
        Integer earnedPoints,
        int maxPoints,
        DimensionStatus status
) {
    public static ScoringDimensionScore applicable(int earnedPoints, int maxPoints) {
        return new ScoringDimensionScore(Math.max(0, earnedPoints), maxPoints, DimensionStatus.APPLICABLE);
    }

    public static ScoringDimensionScore notApplicable(int maxPoints) {
        return new ScoringDimensionScore(null, maxPoints, DimensionStatus.NOT_APPLICABLE);
    }

    public boolean isApplicable() {
        return status == DimensionStatus.APPLICABLE;
    }

    public int earnedOrZero() {
        return earnedPoints == null ? 0 : earnedPoints;
    }
}
