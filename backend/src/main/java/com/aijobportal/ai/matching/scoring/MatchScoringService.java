package com.aijobportal.ai.matching.scoring;

import com.aijobportal.ai.matching.education.EducationMatchingService;
import com.aijobportal.ai.matching.experience.ExperienceMatchingService;
import com.aijobportal.ai.matching.model.NormalizedCandidateProfile;
import com.aijobportal.ai.matching.model.NormalizedJobRequirements;
import com.aijobportal.ai.matching.policy.MatchScorePolicy;
import com.aijobportal.ai.matching.role.RoleMatchingService;
import com.aijobportal.ai.matching.skill.SkillMatchDetail;
import com.aijobportal.ai.matching.skill.SkillMatchingService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class MatchScoringService {

    private final MatchScorePolicy policy;
    private final SkillMatchingService skillMatchingService;
    private final ExperienceMatchingService experienceMatchingService;
    private final RoleMatchingService roleMatchingService;
    private final EducationMatchingService educationMatchingService;

    public MatchScoringService(
            MatchScorePolicy policy,
            SkillMatchingService skillMatchingService,
            ExperienceMatchingService experienceMatchingService,
            RoleMatchingService roleMatchingService,
            EducationMatchingService educationMatchingService
    ) {
        this.policy = policy;
        this.skillMatchingService = skillMatchingService;
        this.experienceMatchingService = experienceMatchingService;
        this.roleMatchingService = roleMatchingService;
        this.educationMatchingService = educationMatchingService;
    }

    public MatchAnalysisResult score(NormalizedCandidateProfile candidate, NormalizedJobRequirements job) {
        List<SkillMatchDetail> criticalMatches = skillMatchingService.matchTier(job.criticalSkills(), candidate.skills());
        List<SkillMatchDetail> requiredMatches = skillMatchingService.matchTier(job.requiredSkills(), candidate.skills());
        List<SkillMatchDetail> preferredMatches = skillMatchingService.matchTier(job.preferredSkills(), candidate.skills());

        int requiredWeight = policy.getWeights().getRequiredSkills();
        int preferredWeight = policy.getWeights().getPreferredSkills();

        int combinedCount = job.combinedRequiredCount();
        double combinedCredits = SkillMatchingService.tierCreditSum(criticalMatches)
                + SkillMatchingService.tierCreditSum(requiredMatches);

        int criticalPoints = 0;
        int requiredPoints = 0;
        if (combinedCount > 0) {
            int totalRequiredBucket = (int) Math.round(requiredWeight * (combinedCredits / combinedCount));
            if (!criticalMatches.isEmpty() && combinedCredits > 0) {
                double criticalCredits = SkillMatchingService.tierCreditSum(criticalMatches);
                criticalPoints = (int) Math.round(totalRequiredBucket * (criticalCredits / combinedCredits));
                requiredPoints = totalRequiredBucket - criticalPoints;
            } else {
                requiredPoints = totalRequiredBucket;
            }
        }

        int preferredCount = job.preferredSkills().size();
        int preferredPoints = preferredCount == 0
                ? 0
                : (int) Math.round(preferredWeight * (SkillMatchingService.tierCreditSum(preferredMatches) / preferredCount));

        int experiencePoints = experienceMatchingService.score(candidate, job);
        int rolePoints = roleMatchingService.score(candidate, job);
        int educationPoints = educationMatchingService.score(candidate, job);

        ScoreBreakdown breakdown = new ScoreBreakdown(
                criticalPoints,
                requiredPoints,
                preferredPoints,
                experiencePoints,
                rolePoints,
                educationPoints
        );

        int raw = breakdown.total();
        boolean capApplied = false;
        String capReason = null;

        if (!job.criticalSkills().isEmpty() && SkillMatchingService.matchedCount(criticalMatches) == 0) {
            int cap = policy.getCaps().getZeroCriticalMaxScore();
            capApplied = true;
            capReason = "Score capped because no critical skills matched.";
            if (raw > cap) {
                raw = cap;
            }
        }

        double requiredRatio = combinedCount == 0 ? 1.0 : combinedCredits / combinedCount;
        if (combinedCount > 0 && requiredRatio < policy.getCaps().getLowRequiredRatioThreshold()) {
            int cap = policy.getCaps().getLowRequiredMaxScore();
            if (raw > cap) {
                raw = cap;
            }
            capApplied = true;
            capReason = capReason == null
                    ? "Score capped because required skill match is below "
                    + (int) (policy.getCaps().getLowRequiredRatioThreshold() * 100) + "%."
                    : capReason + " Also capped for low required skill match.";
        }

        int finalScore = clamp(round(raw), 0, 100);
        ScoreBreakdown scaledBreakdown = scaleBreakdown(breakdown, breakdown.total(), finalScore);

        return new MatchAnalysisResult(
                finalScore,
                scaledBreakdown,
                criticalMatches,
                requiredMatches,
                preferredMatches,
                capApplied,
                capReason
        );
    }

    private ScoreBreakdown scaleBreakdown(ScoreBreakdown breakdown, int rawTotal, int targetTotal) {
        if (rawTotal <= 0 || rawTotal == targetTotal) {
            return breakdown;
        }
        double factor = targetTotal / (double) rawTotal;
        return new ScoreBreakdown(
                scale(breakdown.criticalRequiredSkills(), factor),
                scale(breakdown.requiredSkills(), factor),
                scale(breakdown.preferredSkills(), factor),
                scale(breakdown.relevantExperience(), factor),
                scale(breakdown.roleRelevance(), factor),
                scale(breakdown.educationAndCertifications(), factor)
        );
    }

    private int scale(int value, double factor) {
        return (int) Math.round(value * factor);
    }

    private int round(double value) {
        RoundingMode mode = policy.getRounding() == null ? RoundingMode.HALF_UP : policy.getRounding();
        return BigDecimal.valueOf(value).setScale(0, mode).intValue();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
