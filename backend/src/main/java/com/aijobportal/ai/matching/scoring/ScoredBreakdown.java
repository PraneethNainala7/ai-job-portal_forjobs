package com.aijobportal.ai.matching.scoring;

public record ScoredBreakdown(
        ScoringDimensionScore requiredSkills,
        ScoringDimensionScore preferredSkills,
        ScoringDimensionScore experience,
        ScoringDimensionScore roleRelevance,
        ScoringDimensionScore education
) {
    public int totalEarnedPoints() {
        return requiredSkills.earnedOrZero()
                + preferredSkills.earnedOrZero()
                + experience.earnedOrZero()
                + roleRelevance.earnedOrZero()
                + education.earnedOrZero();
    }

    public int applicableMaximumPoints() {
        int total = 0;
        if (requiredSkills.isApplicable()) {
            total += requiredSkills.maxPoints();
        }
        if (preferredSkills.isApplicable()) {
            total += preferredSkills.maxPoints();
        }
        if (experience.isApplicable()) {
            total += experience.maxPoints();
        }
        if (roleRelevance.isApplicable()) {
            total += roleRelevance.maxPoints();
        }
        if (education.isApplicable()) {
            total += education.maxPoints();
        }
        return total;
    }

    /** Legacy flat earned points for backward-compatible DTO mapping. */
    public ScoreBreakdown toLegacyEarnedBreakdown(int criticalPoints, int requiredPoints) {
        return new ScoreBreakdown(
                criticalPoints,
                requiredPoints,
                preferredSkills.earnedOrZero(),
                experience.earnedOrZero(),
                roleRelevance.earnedOrZero(),
                education.earnedOrZero()
        );
    }
}
