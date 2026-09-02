package com.aijobportal.candidate.service;

import com.aijobportal.ai.matching.normalization.SkillNormalizationService;
import com.aijobportal.candidate.dto.ResumeAnalysisResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Assigns each extracted resume skill to exactly one bucket using a fixed priority order.
 */
public final class ResumeSkillBucketNormalizer {

    private static final SkillNormalizationService DEFAULT_NORMALIZATION = new SkillNormalizationService();

    private ResumeSkillBucketNormalizer() {
    }

    public static ResumeAnalysisResponse normalize(ResumeAnalysisResponse raw) {
        return normalize(raw, DEFAULT_NORMALIZATION);
    }

    public static ResumeAnalysisResponse normalize(ResumeAnalysisResponse raw, SkillNormalizationService normalizationService) {
        if (raw == null) {
            return null;
        }
        Set<String> claimed = new HashSet<>();
        List<String> programmingLanguages = claimBucket(raw.programmingLanguages(), claimed, normalizationService);
        List<String> frameworks = claimBucket(raw.frameworks(), claimed, normalizationService);
        List<String> databases = claimBucket(raw.databases(), claimed, normalizationService);
        List<String> cloudTechnologies = claimBucket(raw.cloudTechnologies(), claimed, normalizationService);
        List<String> tools = claimBucket(raw.tools(), claimed, normalizationService);
        List<String> skills = claimBucket(raw.skills(), claimed, normalizationService);
        List<String> additionalSkills = claimBucket(raw.additionalSkills(), claimed, normalizationService);
        List<String> technologies = claimBucket(raw.technologies(), claimed, normalizationService);
        return new ResumeAnalysisResponse(
                raw.status(),
                raw.fileName(),
                raw.uploadedAt(),
                skills,
                additionalSkills,
                raw.experience(),
                raw.titles(),
                raw.education(),
                raw.certifications(),
                raw.seniority(),
                technologies,
                raw.projects(),
                raw.industries(),
                programmingLanguages,
                frameworks,
                databases,
                cloudTechnologies,
                tools,
                raw.error()
        );
    }

    private static List<String> claimBucket(
            List<String> bucket,
            Set<String> claimed,
            SkillNormalizationService normalizationService
    ) {
        if (bucket == null || bucket.isEmpty()) {
            return List.of();
        }
        List<String> kept = new ArrayList<>();
        for (String skill : bucket) {
            if (skill == null || skill.isBlank()) {
                continue;
            }
            String key = normalizationService.normalizeKey(skill);
            if (key.isBlank() || claimed.contains(key)) {
                continue;
            }
            claimed.add(key);
            kept.add(skill.trim());
        }
        return kept;
    }
}
