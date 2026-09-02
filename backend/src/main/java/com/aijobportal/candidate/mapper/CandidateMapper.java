package com.aijobportal.candidate.mapper;

import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.candidate.dto.ResumeAnalysisResponse;
import com.aijobportal.candidate.entity.CandidateProfile;
import com.aijobportal.candidate.entity.Resume;
import com.aijobportal.candidate.service.ResumeSkillBucketNormalizer;
import com.aijobportal.common.domain.ResumeStatus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CandidateMapper {

    private static final List<String> EMPTY = List.of();

    private CandidateMapper() {
    }

    public static List<String> allExtractedSkills(ResumeAnalysisResponse analysis) {
        if (analysis == null) {
            return List.of();
        }
        List<String> merged = new ArrayList<>();
        appendAll(merged, analysis.skills());
        appendAll(merged, analysis.additionalSkills());
        appendAll(merged, analysis.technologies());
        appendAll(merged, analysis.programmingLanguages());
        appendAll(merged, analysis.frameworks());
        appendAll(merged, analysis.databases());
        appendAll(merged, analysis.cloudTechnologies());
        appendAll(merged, analysis.tools());
        return dedupeIgnoreCase(merged);
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
            return emptyResumeResponse(
                    ResumeStatus.FAILED.name(),
                    resume.getFileName(),
                    resume.getCreatedAt() == null ? null : resume.getCreatedAt().toString(),
                    "Stored resume analysis is outdated. Re-run AI analysis after confirming ANTHROPIC_API_KEY is set."
            );
        }
        if (resume.getStatus() != ResumeStatus.COMPLETE || !hasParsedAnalysis(resume.getParsedData())) {
            return emptyResumeResponse(
                    resume.getStatus().name(),
                    resume.getFileName(),
                    resume.getCreatedAt() == null ? null : resume.getCreatedAt().toString(),
                    resume.getError()
            );
        }
        Map<String, Object> data = resume.getParsedData();
        return ResumeSkillBucketNormalizer.normalize(new ResumeAnalysisResponse(
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
                stringList(data, "programmingLanguages"),
                stringList(data, "frameworks"),
                stringList(data, "databases"),
                stringList(data, "cloudTechnologies"),
                stringList(data, "tools"),
                resume.getError()
        ));
    }

    public static Map<String, Object> emptyParsedData() {
        return toParsedData(emptyResumeResponse(ResumeStatus.PROCESSING.name(), null, null, null));
    }

    private static ResumeAnalysisResponse emptyResumeResponse(
            String status,
            String fileName,
            String uploadedAt,
            String error
    ) {
        return new ResumeAnalysisResponse(
                status,
                fileName,
                uploadedAt,
                EMPTY,
                EMPTY,
                null,
                EMPTY,
                EMPTY,
                EMPTY,
                null,
                EMPTY,
                EMPTY,
                EMPTY,
                EMPTY,
                EMPTY,
                EMPTY,
                EMPTY,
                EMPTY,
                error
        );
    }

    private static boolean hasParsedAnalysis(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return false;
        }
        return !stringList(data, "skills").isEmpty()
                || !stringList(data, "additionalSkills").isEmpty()
                || !stringList(data, "titles").isEmpty()
                || !stringList(data, "technologies").isEmpty()
                || !stringList(data, "programmingLanguages").isEmpty()
                || !stringList(data, "frameworks").isEmpty()
                || !stringList(data, "tools").isEmpty()
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
        data.put("programmingLanguages", orEmpty(analysis.programmingLanguages()));
        data.put("frameworks", orEmpty(analysis.frameworks()));
        data.put("databases", orEmpty(analysis.databases()));
        data.put("cloudTechnologies", orEmpty(analysis.cloudTechnologies()));
        data.put("tools", orEmpty(analysis.tools()));
        data.put("experienceYears", Map.of());
        return data;
    }

    public static CandidateProfileResponse withResumeSkills(CandidateProfileResponse profile, Resume resume) {
        if (resume == null || resume.getStatus() != ResumeStatus.COMPLETE) {
            return profile;
        }
        ResumeAnalysisResponse analysis = toResume(resume);
        List<String> merged = new ArrayList<>(profile.skills() == null ? List.of() : profile.skills());
        merged.addAll(allExtractedSkills(analysis));
        return new CandidateProfileResponse(
                profile.id(), profile.fullName(), profile.email(), profile.phone(), profile.location(),
                profile.title(), profile.experience(), dedupeIgnoreCase(merged), profile.education(),
                profile.certifications(), profile.portfolioUrl(), profile.linkedinUrl()
        );
    }

    private static List<String> orEmpty(List<String> values) {
        return values == null ? List.of() : values;
    }

    private static void appendAll(List<String> target, List<String> values) {
        if (values != null) {
            target.addAll(values);
        }
    }

    private static List<String> dedupeIgnoreCase(List<String> values) {
        List<String> unique = new ArrayList<>();
        for (String skill : values) {
            if (skill == null || skill.isBlank()) {
                continue;
            }
            if (unique.stream().noneMatch(item -> item.equalsIgnoreCase(skill))) {
                unique.add(skill.trim());
            }
        }
        return unique;
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
