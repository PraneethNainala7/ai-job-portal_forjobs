package com.aijobportal.ai.matching;

import java.util.List;

public record SkillMatchResult(
        List<String> matchedRequired,
        List<String> missingRequired,
        List<String> matchedPreferred,
        List<String> missingPreferred,
        List<String> partiallyRelevant
) {
    public SkillMatchResult {
        matchedRequired = matchedRequired == null ? List.of() : List.copyOf(matchedRequired);
        missingRequired = missingRequired == null ? List.of() : List.copyOf(missingRequired);
        matchedPreferred = matchedPreferred == null ? List.of() : List.copyOf(matchedPreferred);
        missingPreferred = missingPreferred == null ? List.of() : List.copyOf(missingPreferred);
        partiallyRelevant = partiallyRelevant == null ? List.of() : List.copyOf(partiallyRelevant);
    }

    public double requiredMatchRatio(int requiredCount) {
        if (requiredCount <= 0) {
            return 0;
        }
        return matchedRequired.size() / (double) requiredCount;
    }
}
