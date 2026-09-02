package com.aijobportal.candidate.mapper;

import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.candidate.dto.ResumeAnalysisResponse;
import com.aijobportal.candidate.entity.CandidateProfile;
import com.aijobportal.candidate.entity.Resume;
import com.aijobportal.common.domain.ResumeStatus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CandidateMapper {

    private CandidateMapper() {
    }

    public static CandidateProfileResponse toProfile(CandidateProfile profile) {
        return new CandidateProfileResponse(
                profile.getUser().getId(),
                profile.getUser().getName(),
                profile.getUser().getEmail(),
                profile.getPhone(),
                profile.getLocation(),
                profile.getTitle(),
                profile.getExperience(),
                profile.getSkills(),
                profile.getEducation(),
                profile.getCertifications(),
                profile.getPortfolioUrl(),
                profile.getLinkedinUrl()
        );
    }

    public static ResumeAnalysisResponse toResume(Resume resume) {
        if (resume.getStatus() == ResumeStatus.COMPLETE && isLegacyHeuristicStub(resume.getParsedData())) {
            return new ResumeAnalysisResponse(
                    ResumeStatus.FAILED.name(),
                    resume.getFileName(),
                    resume.getCreatedAt() == null ? null : resume.getCreatedAt().toString(),
                    List.of(),
                    List.of(),
                    null,
                    List.of(),
                    List.of(),
                    List.of(),
                    null,
                    List.of(),
                    List.of(),
                    List.of(),
                    "Stored resume analysis is outdated. Re-run AI analysis after confirming ANTHROPIC_API_KEY is set."
            );
        }
        if (resume.getStatus() != ResumeStatus.COMPLETE || !hasParsedAnalysis(resume.getParsedData())) {
            return new ResumeAnalysisResponse(
                    resume.getStatus().name(),
                    resume.getFileName(),
                    resume.getCreatedAt() == null ? null : resume.getCreatedAt().toString(),
                    List.of(),
                    List.of(),
                    null,
                    List.of(),
                    List.of(),
                    List.of(),
                    null,
                    List.of(),
                    List.of(),
                    List.of(),
                    resume.getError()
            );
        }
        Map<String, Object> data = resume.getParsedData();
        return new ResumeAnalysisResponse(
                resume.getStatus().name(),
                resume.getFileName(),
                resume.getCreatedAt() == null ? null : resume.getCreatedAt().toString(),
                stringList(data, "skills"),
                stringList(data, "additionalSkills"),
                stringValue(data, "experience"),
                stringList(data, "titles"),
                stringList(data, "education"),
                stringList(data, "certifications"),
                stringValue(data, "seniority"),
                stringList(data, "technologies"),
                stringList(data, "projects"),
                stringList(data, "industries"),
                resume.getError()
        );
    }

    public static Map<String, Object> emptyParsedData() {
        return toParsedData(new ResumeAnalysisResponse(
                ResumeStatus.PROCESSING.name(),
                null,
                null,
                List.of(),
                List.of(),
                null,
                List.of(),
                List.of(),
                List.of(),
                null,
                List.of(),
                List.of(),
                List.of(),
                null
        ));
    }

    private static boolean hasParsedAnalysis(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return false;
        }
        return !stringList(data, "skills").isEmpty()
                || !stringList(data, "additionalSkills").isEmpty()
                || !stringList(data, "titles").isEmpty()
                || !stringList(data, "technologies").isEmpty()
                || stringValue(data, "experience") != null;
    }

    private static boolean isLegacyHeuristicStub(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return false;
        }
        return stringList(data, "skills").equals(List.of("Communication", "Problem solving"))
                && stringList(data, "additionalSkills").equals(List.of("Git", "Agile"))
                && "Mid-level".equals(stringValue(data, "seniority"))
                && stringList(data, "projects").equals(List.of("Professional project history"))
                && stringList(data, "industries").equals(List.of("Technology"));
    }

    public static Map<String, Object> toParsedData(ResumeAnalysisResponse analysis) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("skills", orEmpty(analysis.skills()));
        data.put("additionalSkills", orEmpty(analysis.additionalSkills()));
        data.put("experience", analysis.experience() == null ? "" : analysis.experience());
        data.put("titles", orEmpty(analysis.titles()));
        data.put("education", orEmpty(analysis.education()));
        data.put("certifications", orEmpty(analysis.certifications()));
        data.put("seniority", analysis.seniority() == null ? "" : analysis.seniority());
        data.put("technologies", orEmpty(analysis.technologies()));
        data.put("projects", orEmpty(analysis.projects()));
        data.put("industries", orEmpty(analysis.industries()));
        data.put("programmingLanguages", List.of());
        data.put("frameworks", List.of());
        data.put("databases", List.of());
        data.put("cloudTechnologies", List.of());
        data.put("tools", List.of());
        data.put("experienceYears", Map.of());
        return data;
    }

    public static CandidateProfileResponse withResumeSkills(CandidateProfileResponse profile, Resume resume) {
        if (resume == null || resume.getStatus() != ResumeStatus.COMPLETE) {
            return profile;
        }
        ResumeAnalysisResponse analysis = toResume(resume);
        List<String> merged = new ArrayList<>(profile.skills() == null ? List.of() : profile.skills());
        if (analysis.skills() != null) {
            merged.addAll(analysis.skills());
        }
        if (analysis.additionalSkills() != null) {
            merged.addAll(analysis.additionalSkills());
        }
        if (analysis.technologies() != null) {
            merged.addAll(analysis.technologies());
        }
        List<String> unique = new ArrayList<>();
        for (String skill : merged) {
            if (unique.stream().noneMatch(item -> item.equalsIgnoreCase(skill))) {
                unique.add(skill);
            }
        }
        return new CandidateProfileResponse(
                profile.id(), profile.fullName(), profile.email(), profile.phone(), profile.location(),
                profile.title(), profile.experience(), unique, profile.education(), profile.certifications(),
                profile.portfolioUrl(), profile.linkedinUrl()
        );
    }

    private static List<String> orEmpty(List<String> values) {
        return values == null ? List.of() : values;
    }

    private static List<String> stringList(Map<String, Object> data, String key) {
        if (data == null || !(data.get(key) instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().map(String::valueOf).toList();
    }

    private static String stringValue(Map<String, Object> data, String key) {
        if (data == null || data.get(key) == null) {
            return null;
        }
        String value = String.valueOf(data.get(key));
        return value.isBlank() ? null : value;
    }
}
