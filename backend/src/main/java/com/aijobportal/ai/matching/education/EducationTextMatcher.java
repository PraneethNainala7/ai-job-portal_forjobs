package com.aijobportal.ai.matching.education;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

final class EducationTextMatcher {

    private EducationTextMatcher() {
    }

    static boolean matches(String requirement, String candidateValue) {
        if (requirement == null || requirement.isBlank() || candidateValue == null || candidateValue.isBlank()) {
            return false;
        }
        String normalizedRequirement = normalize(requirement);
        String normalizedCandidate = normalize(candidateValue);
        if (normalizedRequirement.isBlank() || normalizedCandidate.isBlank()) {
            return false;
        }
        if (normalizedCandidate.contains(normalizedRequirement) || normalizedRequirement.contains(normalizedCandidate)) {
            return true;
        }
        Set<String> requirementTokens = tokens(normalizedRequirement);
        Set<String> candidateTokens = tokens(normalizedCandidate);
        if (requirementTokens.isEmpty() || candidateTokens.isEmpty()) {
            return false;
        }
        long overlap = requirementTokens.stream().filter(candidateTokens::contains).count();
        return overlap >= Math.max(1, (int) Math.ceil(requirementTokens.size() * 0.6));
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static Set<String> tokens(String normalized) {
        return Arrays.stream(normalized.split("\\s+"))
                .filter(token -> token.length() > 1)
                .collect(Collectors.toSet());
    }
}
