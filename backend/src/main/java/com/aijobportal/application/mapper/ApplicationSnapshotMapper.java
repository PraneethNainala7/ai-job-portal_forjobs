package com.aijobportal.application.mapper;

import com.aijobportal.ai.dto.MatchResultResponse;
import com.aijobportal.application.entity.JobApplication;
import com.aijobportal.auth.entity.User;
import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.candidate.entity.CandidateProfile;
import com.aijobportal.candidate.mapper.CandidateMapper;
import com.aijobportal.employer.dto.ApplicationResumeInfo;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ApplicationSnapshotMapper {

    private ApplicationSnapshotMapper() {
    }

    public static boolean hasSnapshot(JobApplication application) {
        return application.getResumeParsedData() != null && !application.getResumeParsedData().isEmpty();
    }

    public static CandidateProfileResponse candidateFromApplication(
            JobApplication application,
            CandidateProfile profile
    ) {
        User user = application.getCandidate();
        if (!hasSnapshot(application)) {
            if (profile == null) {
                return new CandidateProfileResponse(
                        user.getId(), user.getName(), user.getEmail(),
                        null, null, null, null, List.of(), List.of(), List.of(), null, null
                );
            }
            return CandidateMapper.toProfile(profile);
        }
        Map<String, Object> data = application.getResumeParsedData();
        List<String> skills = mergeSkills(stringList(data, "skills"), stringList(data, "additionalSkills"));
        List<String> titles = stringList(data, "titles");
        String title = profile == null || profile.getTitle() == null || profile.getTitle().isBlank()
                ? titles.isEmpty() ? null : titles.getFirst()
                : profile.getTitle();
        String experience = stringValue(data, "experience");
        if (experience == null && profile != null) {
            experience = profile.getExperience();
        }
        return new CandidateProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                profile == null ? null : profile.getPhone(),
                profile == null ? null : profile.getLocation(),
                title,
                experience,
                skills,
                stringList(data, "education").isEmpty() && profile != null
                        ? profile.getEducation()
                        : stringList(data, "education"),
                stringList(data, "certifications").isEmpty() && profile != null
                        ? profile.getCertifications()
                        : stringList(data, "certifications"),
                profile == null ? null : profile.getPortfolioUrl(),
                profile == null ? null : profile.getLinkedinUrl()
        );
    }

    public static MatchResultResponse matchFromApplication(JobApplication application) {
        if (application.getMatchScore() == null) {
            return null;
        }
        return new MatchResultResponse(
                application.getJob().getId(),
                application.getCandidate().getId(),
                application.getMatchScore(),
                orEmpty(application.getMatchStrongAreas()),
                orEmpty(application.getMatchGaps()),
                application.getMatchExplanation()
        );
    }

    public static ApplicationResumeInfo resumeInfo(JobApplication application) {
        if (application.getResumeFileUrl() == null || application.getResumeFileUrl().isBlank()) {
            return null;
        }
        return new ApplicationResumeInfo(
                application.getResumeFileName(),
                application.getAppliedAt().toString(),
                true
        );
    }

    private static List<String> mergeSkills(List<String> primary, List<String> additional) {
        Set<String> seen = new LinkedHashSet<>();
        List<String> merged = new ArrayList<>();
        for (String skill : primary) {
            String key = skill.toLowerCase();
            if (seen.add(key)) {
                merged.add(skill);
            }
        }
        for (String skill : additional) {
            String key = skill.toLowerCase();
            if (seen.add(key)) {
                merged.add(skill);
            }
        }
        return merged;
    }

    private static List<String> orEmpty(List<String> values) {
        return values == null ? List.of() : values;
    }

    private static List<String> stringList(Map<String, Object> data, String key) {
        if (data == null || !(data.get(key) instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().map(String::valueOf).filter(item -> !item.isBlank()).toList();
    }

    private static String stringValue(Map<String, Object> data, String key) {
        if (data == null || data.get(key) == null) {
            return null;
        }
        String value = String.valueOf(data.get(key));
        return value.isBlank() ? null : value;
    }
}
