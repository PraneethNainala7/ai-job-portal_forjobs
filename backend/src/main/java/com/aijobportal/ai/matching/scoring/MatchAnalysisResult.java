package com.aijobportal.ai.matching.scoring;

import com.aijobportal.ai.matching.skill.SkillMatchDetail;

import java.util.List;

public record MatchAnalysisResult(
        int totalScore,
        ScoreBreakdown scoreBreakdown,
        List<SkillMatchDetail> criticalMatches,
        List<SkillMatchDetail> requiredMatches,
        List<SkillMatchDetail> preferredMatches,
        boolean scoreCapApplied,
        String scoreCapReason
) {
}
