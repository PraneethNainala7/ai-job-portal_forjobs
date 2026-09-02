package com.aijobportal.ai.matching;

import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.candidate.dto.ResumeAnalysisResponse;
import com.aijobportal.candidate.entity.Resume;
import com.aijobportal.candidate.mapper.CandidateMapper;
import com.aijobportal.common.domain.ResumeStatus;

import java.util.ArrayList;
import java.util.List;

public record CandidateSkillProfile(
        String candidateId,
        String title,
        String experience,
        List<String> skills,
        List<String> education,
        List<String> certifications,
        List<String> titles
) {
    public static CandidateSkillProfile from(CandidateProfileResponse profile, Resume resume) {
        CandidateProfileResponse merged = profile;
        if (resume != null && resume.getStatus() == ResumeStatus.COMPLETE) {
            merged = CandidateMapper.withResumeSkills(profile, resume);
        }
        ResumeAnalysisResponse analysis = resume == null ? null : CandidateMapper.toResume(resume);
        List<String> allSkills = new ArrayList<>(merged.skills() == null ? List.of() : merged.skills());
        if (analysis != null && analysis.technologies() != null) {
            allSkills.addAll(analysis.technologies());
        }
        List<String> titles = analysis == null || analysis.titles() == null ? List.of() : analysis.titles();
        return new CandidateSkillProfile(
                merged.id(),
                merged.title(),
                merged.experience(),
                SkillNormalizer.dedupe(allSkills),
                merged.education() == null ? List.of() : merged.education(),
                merged.certifications() == null ? List.of() : merged.certifications(),
                titles
        );
    }
}
