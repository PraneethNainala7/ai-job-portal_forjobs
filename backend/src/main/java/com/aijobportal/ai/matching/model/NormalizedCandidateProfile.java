package com.aijobportal.ai.matching.model;

import com.aijobportal.ai.matching.normalization.SkillNormalizationService;
import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.candidate.dto.ResumeAnalysisResponse;
import com.aijobportal.candidate.entity.Resume;
import com.aijobportal.candidate.mapper.CandidateMapper;
import com.aijobportal.common.domain.ResumeStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record NormalizedCandidateProfile(
        String candidateId,
        String title,
        String experience,
        List<String> skills,
        List<String> education,
        List<String> certifications,
        List<String> titles,
        Map<String, Integer> experienceYearsByDomain
) {
    public static NormalizedCandidateProfile from(
            CandidateProfileResponse profile,
            Resume resume,
            SkillNormalizationService normalizationService
    ) {
        CandidateProfileResponse merged = profile;
        if (resume != null && resume.getStatus() == ResumeStatus.COMPLETE) {
            merged = CandidateMapper.withResumeSkills(profile, resume);
        }
        ResumeAnalysisResponse analysis = resume == null ? null : CandidateMapper.toResume(resume);
        List<String> allSkills = new ArrayList<>(merged.skills() == null ? List.of() : merged.skills());
        if (analysis != null) {
            appendAll(allSkills, analysis.technologies());
            appendAll(allSkills, stringListFromParsed(resume, "programmingLanguages"));
            appendAll(allSkills, stringListFromParsed(resume, "frameworks"));
            appendAll(allSkills, stringListFromParsed(resume, "databases"));
            appendAll(allSkills, stringListFromParsed(resume, "cloudTechnologies"));
            appendAll(allSkills, stringListFromParsed(resume, "tools"));
        }
        List<String> titles = analysis == null || analysis.titles() == null ? List.of() : analysis.titles();
        Map<String, Integer> experienceByDomain = parseExperienceByDomain(resume);
        return new NormalizedCandidateProfile(
                merged.id(),
                merged.title(),
                merged.experience(),
                normalizationService.dedupe(allSkills),
                merged.education() == null ? List.of() : merged.education(),
                merged.certifications() == null ? List.of() : merged.certifications(),
                titles,
                experienceByDomain
        );
    }

    @SuppressWarnings("unchecked")
    private static List<String> stringListFromParsed(Resume resume, String key) {
        if (resume == null || resume.getParsedData() == null) {
            return List.of();
        }
        Object value = resume.getParsedData().get(key);
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().map(String::valueOf).filter(item -> !item.isBlank()).toList();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Integer> parseExperienceByDomain(Resume resume) {
        Map<String, Integer> result = new HashMap<>();
        if (resume == null || resume.getParsedData() == null) {
            return result;
        }
        Object value = resume.getParsedData().get("experienceYears");
        if (value instanceof Map<?, ?> map) {
            map.forEach((key, years) -> {
                if (key != null && years != null) {
                    try {
                        result.put(String.valueOf(key), Integer.parseInt(String.valueOf(years)));
                    } catch (NumberFormatException ignored) {
                        // skip invalid values
                    }
                }
            });
        }
        return result;
    }

    private static void appendAll(List<String> target, List<String> values) {
        if (values != null) {
            target.addAll(values);
        }
    }
}
