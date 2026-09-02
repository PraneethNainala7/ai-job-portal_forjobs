package com.aijobportal.ai.matching;

import com.aijobportal.ai.dto.MatchResultResponse;
import com.aijobportal.ai.dto.MatchScoreBreakdownDto;
import com.aijobportal.ai.dto.ScoreBreakdownResponse;
import com.aijobportal.ai.dto.SkillMatchResponse;
import com.aijobportal.ai.matching.model.NormalizedCandidateProfile;
import com.aijobportal.ai.matching.model.NormalizedJobRequirements;
import com.aijobportal.ai.matching.normalization.SkillNormalizationService;
import com.aijobportal.ai.matching.scoring.MatchAnalysisResult;
import com.aijobportal.ai.matching.scoring.MatchScoringService;
import com.aijobportal.ai.matching.skill.MatchType;
import com.aijobportal.ai.matching.skill.SkillMatchDetail;
import com.aijobportal.ai.matching.skill.SkillMatchingService;
import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.candidate.entity.Resume;
import com.aijobportal.job.entity.Job;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MatchAnalysisService {

    private final MatchScoringService matchScoringService;
    private final SkillNormalizationService normalizationService;

    public MatchAnalysisService(
            MatchScoringService matchScoringService,
            SkillNormalizationService normalizationService
    ) {
        this.matchScoringService = matchScoringService;
        this.normalizationService = normalizationService;
    }

    public MatchResultResponse analyze(Job job, CandidateProfileResponse profile, Resume resume) {
        NormalizedCandidateProfile candidate = NormalizedCandidateProfile.from(profile, resume, normalizationService);
        NormalizedJobRequirements requirements = NormalizedJobRequirements.from(job);
        MatchAnalysisResult result = matchScoringService.score(candidate, requirements);
        return toResponse(job.getId(), candidate.candidateId(), result);
    }

    public List<MatchResultResponse> rankAll(CandidateProfileResponse profile, List<Job> jobs, Resume resume) {
        if (jobs == null || jobs.isEmpty()) {
            return List.of();
        }
        NormalizedCandidateProfile candidate = NormalizedCandidateProfile.from(profile, resume, normalizationService);
        List<MatchResultResponse> results = new ArrayList<>();
        for (Job job : jobs) {
            NormalizedJobRequirements requirements = NormalizedJobRequirements.from(job);
            MatchAnalysisResult result = matchScoringService.score(candidate, requirements);
            results.add(toResponse(job.getId(), candidate.candidateId(), result));
        }
        return results;
    }

    private static MatchResultResponse toResponse(String jobId, String candidateId, MatchAnalysisResult result) {
        List<SkillMatchDetail> allRequired = new ArrayList<>(result.criticalMatches());
        allRequired.addAll(result.requiredMatches());

        List<String> matchedSkills = new ArrayList<>();
        matchedSkills.addAll(exactMatchedLabels(result.criticalMatches()));
        matchedSkills.addAll(exactMatchedLabels(result.requiredMatches()));
        matchedSkills.addAll(exactMatchedLabels(result.preferredMatches()));

        List<String> partiallyRelevant = SkillMatchingService.partiallyMatchedLabels(allRequired);
        partiallyRelevant.addAll(SkillMatchingService.partiallyMatchedLabels(result.preferredMatches()));

        List<String> missingRequired = SkillMatchingService.missingSkills(result.requiredMatches());
        List<String> missingCritical = SkillMatchingService.missingSkills(result.criticalMatches());
        List<String> missingPreferred = SkillMatchingService.missingSkills(result.preferredMatches());
        List<String> matchedPreferred = SkillMatchingService.matchedSkillLabels(result.preferredMatches());

        List<String> strongAreas = new ArrayList<>(matchedSkills);
        strongAreas.addAll(partiallyRelevant);

        List<String> gaps = new ArrayList<>(missingCritical);
        gaps.addAll(missingRequired);

        String explanation = buildExplanation(result, missingCritical, missingRequired, matchedSkills);

        return new MatchResultResponse(
                jobId,
                candidateId,
                result.totalScore(),
                strongAreas,
                gaps,
                explanation,
                matchedSkills,
                partiallyRelevant,
                missingRequired,
                missingPreferred,
                MatchScoreBreakdownDto.from(result.scoreBreakdown()),
                toSkillResponses(exactMatches(allRequired, result.preferredMatches())),
                toSkillResponses(partialMatches(allRequired, result.preferredMatches())),
                missingCritical,
                matchedPreferred,
                result.scoreCapApplied(),
                result.scoreCapReason(),
                ScoreBreakdownResponse.from(result.scoreBreakdown())
        );
    }

    private static String buildExplanation(
            MatchAnalysisResult result,
            List<String> missingCritical,
            List<String> missingRequired,
            List<String> matchedSkills
    ) {
        if (matchedSkills.isEmpty()) {
            return "Limited match: no required skills matched. "
                    + formatMissing(missingCritical, missingRequired)
                    + (result.scoreCapApplied() && result.scoreCapReason() != null
                    ? " " + result.scoreCapReason()
                    : "");
        }
        return "Matched skills: "
                + String.join(", ", matchedSkills)
                + ". "
                + formatMissing(missingCritical, missingRequired)
                + (result.scoreCapApplied() && result.scoreCapReason() != null
                ? " " + result.scoreCapReason()
                : "");
    }

    private static String formatMissing(List<String> missingCritical, List<String> missingRequired) {
        List<String> parts = new ArrayList<>();
        if (!missingCritical.isEmpty()) {
            parts.add("Missing critical: " + String.join(", ", missingCritical));
        }
        if (!missingRequired.isEmpty()) {
            parts.add("Missing required: " + String.join(", ", missingRequired));
        }
        if (parts.isEmpty()) {
            return "No skill gaps identified.";
        }
        return String.join(". ", parts) + ".";
    }

    private static List<String> exactMatchedLabels(List<SkillMatchDetail> details) {
        List<String> labels = new ArrayList<>();
        for (SkillMatchDetail detail : details) {
            if (detail.matchType() == MatchType.EXACT_MATCH) {
                labels.add(detail.jobSkill());
            }
        }
        return labels;
    }

    private static List<SkillMatchDetail> exactMatches(List<SkillMatchDetail>... tiers) {
        List<SkillMatchDetail> matches = new ArrayList<>();
        for (List<SkillMatchDetail> tier : tiers) {
            for (SkillMatchDetail detail : tier) {
                if (detail.matchType() == MatchType.EXACT_MATCH) {
                    matches.add(detail);
                }
            }
        }
        return matches;
    }

    private static List<SkillMatchDetail> partialMatches(List<SkillMatchDetail>... tiers) {
        List<SkillMatchDetail> matches = new ArrayList<>();
        for (List<SkillMatchDetail> tier : tiers) {
            for (SkillMatchDetail detail : tier) {
                if (detail.matchType() == MatchType.RELATED_MATCH || detail.matchType() == MatchType.TRANSFERABLE_MATCH) {
                    matches.add(detail);
                }
            }
        }
        return matches;
    }

    private static List<SkillMatchResponse> toSkillResponses(List<SkillMatchDetail> details) {
        return details.stream()
                .map(detail -> new SkillMatchResponse(
                        detail.jobSkill(),
                        detail.candidateSkill(),
                        detail.matchType().name()
                ))
                .toList();
    }
}
