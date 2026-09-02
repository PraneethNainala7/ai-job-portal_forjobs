package com.aijobportal.ai.matching.scoring;

import com.aijobportal.ai.matching.skill.SkillMatchDetail;

import java.util.List;

public record MatchAnalysisResult(
        int totalScore,
        ScoreBreakdown scoreBreakdown,
        ScoredBreakdown scoredBreakdown,
        List<SkillMatchDetail> criticalMatches,
        List<SkillMatchDetail> requiredMatches,
        List<SkillMatchDetail> preferredMatches,
        boolean scoreCapApplied,
        String scoreCapReason,
        boolean scoreReliable,
        String scoreUnreliableReason,
        int totalEarnedPoints,
        int applicableMaximumPoints
) {
}
