package com.aijobportal.ai.matching;

public record MatchScoreBreakdown(
        int requiredSkills,
        int preferredSkills,
        int experience,
        int roleRelevance,
        int semanticSimilarity,
        int education
) {
    public int total() {
        return Math.max(0, Math.min(100,
                requiredSkills + preferredSkills + experience + roleRelevance + semanticSimilarity + education));
    }
}
