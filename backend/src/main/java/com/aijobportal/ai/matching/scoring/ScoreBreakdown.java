package com.aijobportal.ai.matching.scoring;

import java.util.List;

public record ScoreBreakdown(
        int criticalRequiredSkills,
        int requiredSkills,
        int preferredSkills,
        int relevantExperience,
        int roleRelevance,
        int educationAndCertifications
) {
    public int total() {
        return criticalRequiredSkills
                + requiredSkills
                + preferredSkills
                + relevantExperience
                + roleRelevance
                + educationAndCertifications;
    }
}
