package com.aijobportal.candidate.mapper;

import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.candidate.dto.ResumeAnalysisResponse;
import com.aijobportal.candidate.entity.CandidateProfile;
import com.aijobportal.candidate.entity.Resume;
import com.aijobportal.common.domain.ResumeStatus;

import java.util.ArrayList;
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

    @SuppressWarnings("unchecked")
    public static ResumeAnalysisResponse toResume(Resume resume) {
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

    public static Map<String, Object> toParsedData(ResumeAnalysisResponse analysis) {
        return Map.of(
                "skills", orEmpty(analysis.skills()),
                "additionalSkills", orEmpty(analysis.additionalSkills()),
                "experience", analysis.experience() == null ? "" : analysis.experience(),
                "titles", orEmpty(analysis.titles()),
                "education", orEmpty(analysis.education()),
                "certifications", orEmpty(analysis.certifications()),
                "seniority", analysis.seniority() == null ? "" : analysis.seniority(),
                "technologies", orEmpty(analysis.technologies()),
                "projects", orEmpty(analysis.projects()),
                "industries", orEmpty(analysis.industries())
        );
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
