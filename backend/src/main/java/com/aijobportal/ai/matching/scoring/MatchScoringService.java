package com.aijobportal.ai.matching.scoring;

import com.aijobportal.ai.matching.education.EducationMatchingService;
import com.aijobportal.ai.matching.experience.ExperienceMatchingService;
import com.aijobportal.ai.matching.model.NormalizedCandidateProfile;
import com.aijobportal.ai.matching.model.NormalizedJobRequirements;
import com.aijobportal.ai.matching.policy.MatchScorePolicy;
import com.aijobportal.ai.matching.role.RoleMatchingService;
import com.aijobportal.ai.matching.skill.JobDescriptionSkillExtractor;
import com.aijobportal.ai.matching.skill.SkillMatchDetail;
import com.aijobportal.ai.matching.skill.SkillMatchingService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class MatchScoringService {

    private final MatchScorePolicy policy;
    private final SkillMatchingService skillMatchingService;
    private final ExperienceMatchingService experienceMatchingService;
    private final RoleMatchingService roleMatchingService;
    private final EducationMatchingService educationMatchingService;
    private final JobDescriptionSkillExtractor descriptionSkillExtractor;

    public MatchScoringService(
            MatchScorePolicy policy,
            SkillMatchingService skillMatchingService,
            ExperienceMatchingService experienceMatchingService,
            RoleMatchingService roleMatchingService,
            EducationMatchingService educationMatchingService,
            JobDescriptionSkillExtractor descriptionSkillExtractor
    ) {
        this.policy = policy;
        this.skillMatchingService = skillMatchingService;
        this.experienceMatchingService = experienceMatchingService;
        this.roleMatchingService = roleMatchingService;
        this.educationMatchingService = educationMatchingService;
        this.descriptionSkillExtractor = descriptionSkillExtractor;
    }

    public MatchAnalysisResult score(NormalizedCandidateProfile candidate, NormalizedJobRequirements job) {
        List<String> extractedSkills = descriptionSkillExtractor.extract(job.role(), job.description());
        List<String> effectiveRequired = mergeRequiredSkills(job, extractedSkills);

        List<String> criticalSkills = job.criticalSkills();
        List<SkillMatchDetail> criticalMatches = skillMatchingService.matchTier(criticalSkills, candidate.skills());
        List<SkillMatchDetail> requiredMatches = skillMatchingService.matchTier(effectiveRequired, candidate.skills());
        List<SkillMatchDetail> preferredMatches = skillMatchingService.matchTier(job.preferredSkills(), candidate.skills());

        int requiredWeight = policy.getWeights().getRequiredSkills();
        int preferredWeight = policy.getWeights().getPreferredSkills();
        int experienceWeight = policy.getWeights().getExperience();
        int roleWeight = policy.getWeights().getRoleRelevance();
        int educationWeight = policy.getWeights().getEducation();

        boolean skillsApplicable = DimensionApplicability.requiredSkillsApplicable(job, extractedSkills);
        int combinedCount = criticalSkills.size() + effectiveRequired.size();
        double combinedCredits = SkillMatchingService.tierCreditSum(criticalMatches)
                + SkillMatchingService.tierCreditSum(requiredMatches);

        int criticalPoints = 0;
        int requiredPoints = 0;
        if (skillsApplicable && combinedCount > 0) {
            int totalRequiredBucket = (int) Math.round(requiredWeight * (combinedCredits / combinedCount));
            if (!criticalMatches.isEmpty() && combinedCredits > 0) {
                double criticalCredits = SkillMatchingService.tierCreditSum(criticalMatches);
                criticalPoints = (int) Math.round(totalRequiredBucket * (criticalCredits / combinedCredits));
                requiredPoints = totalRequiredBucket - criticalPoints;
            } else {
                requiredPoints = totalRequiredBucket;
            }
        }

        int requiredBucketEarned = criticalPoints + requiredPoints;
        ScoringDimensionScore requiredDimension = skillsApplicable
                ? ScoringDimensionScore.applicable(requiredBucketEarned, requiredWeight)
                : ScoringDimensionScore.notApplicable(requiredWeight);

        ScoringDimensionScore preferredDimension;
        if (DimensionApplicability.preferredSkillsApplicable(job)) {
            int preferredCount = job.preferredSkills().size();
            int preferredPoints = (int) Math.round(
                    preferredWeight * (SkillMatchingService.tierCreditSum(preferredMatches) / preferredCount)
            );
            preferredDimension = ScoringDimensionScore.applicable(preferredPoints, preferredWeight);
        } else {
            preferredDimension = ScoringDimensionScore.notApplicable(preferredWeight);
        }

        ScoringDimensionScore experienceDimension = DimensionApplicability.experienceApplicable(job)
                ? ScoringDimensionScore.applicable(experienceMatchingService.score(candidate, job), experienceWeight)
                : ScoringDimensionScore.notApplicable(experienceWeight);

        ScoringDimensionScore roleDimension = DimensionApplicability.roleApplicable(job)
                ? ScoringDimensionScore.applicable(roleMatchingService.score(candidate, job), roleWeight)
                : ScoringDimensionScore.notApplicable(roleWeight);

        ScoringDimensionScore educationDimension = DimensionApplicability.educationApplicable(job)
                ? ScoringDimensionScore.applicable(educationMatchingService.score(candidate, job), educationWeight)
                : ScoringDimensionScore.notApplicable(educationWeight);

        ScoredBreakdown scoredBreakdown = new ScoredBreakdown(
                requiredDimension,
                preferredDimension,
                experienceDimension,
                roleDimension,
                educationDimension
        );

        int applicableMaximum = scoredBreakdown.applicableMaximumPoints();
        if (applicableMaximum <= 0) {
            return new MatchAnalysisResult(
                    0,
                    scoredBreakdown.toLegacyEarnedBreakdown(criticalPoints, requiredPoints),
                    scoredBreakdown,
                    criticalMatches,
                    requiredMatches,
                    preferredMatches,
                    false,
                    null,
                    false,
                    "Analysis cannot produce a reliable match score because the job does not contain sufficient requirements.",
                    0,
                    0
            );
        }

        int earnedPoints = scoredBreakdown.totalEarnedPoints();
        double normalized = (earnedPoints / (double) applicableMaximum) * 100.0;

        boolean capApplied = false;
        String capReason = null;

        if (!criticalSkills.isEmpty() && SkillMatchingService.matchedCount(criticalMatches) == 0) {
            int cap = policy.getCaps().getZeroCriticalMaxScore();
            capApplied = true;
            capReason = "Score capped because no critical skills matched.";
            normalized = Math.min(normalized, cap);
        }

        if (skillsApplicable && combinedCount > 0) {
            double requiredRatio = combinedCredits / combinedCount;
            if (requiredRatio < policy.getCaps().getLowRequiredRatioThreshold()) {
                int cap = policy.getCaps().getLowRequiredMaxScore();
                normalized = Math.min(normalized, cap);
                capApplied = true;
                capReason = capReason == null
                        ? "Score capped because required skill match is below "
                        + (int) (policy.getCaps().getLowRequiredRatioThreshold() * 100) + "%."
                        : capReason + " Also capped for low required skill match.";
            }
        }

        int finalScore = clamp(round(normalized), 0, 100);

        return new MatchAnalysisResult(
                finalScore,
                scoredBreakdown.toLegacyEarnedBreakdown(criticalPoints, requiredPoints),
                scoredBreakdown,
                criticalMatches,
                requiredMatches,
                preferredMatches,
                capApplied,
                capReason,
                true,
                null,
                earnedPoints,
                applicableMaximum
        );
    }

    private static List<String> mergeRequiredSkills(NormalizedJobRequirements job, List<String> extractedSkills) {
        if (!job.requiredSkills().isEmpty()) {
            return job.requiredSkills();
        }
        if (!job.criticalSkills().isEmpty()) {
            return List.of();
        }
        return extractedSkills;
    }

    private int round(double value) {
        RoundingMode mode = policy.getRounding() == null ? RoundingMode.HALF_UP : policy.getRounding();
        return BigDecimal.valueOf(value).setScale(0, mode).intValue();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
