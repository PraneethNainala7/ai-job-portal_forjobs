package com.aijobportal.ai.matching.skill;

public record SkillMatchDetail(
        String jobSkill,
        String candidateSkill,
        MatchType matchType,
        double creditMultiplier
) {
    public boolean hasCredit() {
        return matchType != MatchType.NO_MATCH && creditMultiplier > 0;
    }
}
