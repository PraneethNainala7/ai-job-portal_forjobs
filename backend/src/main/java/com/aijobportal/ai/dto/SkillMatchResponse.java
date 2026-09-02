package com.aijobportal.ai.dto;

public record SkillMatchResponse(
        String jobSkill,
        String candidateSkill,
        String matchType
) {
}
