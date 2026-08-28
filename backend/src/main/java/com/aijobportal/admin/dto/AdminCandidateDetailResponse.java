package com.aijobportal.admin.dto;

import com.aijobportal.auth.dto.UserResponse;
import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.candidate.dto.ResumeAnalysisResponse;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminCandidateDetailResponse(
        UserResponse account,
        CandidateProfileResponse profile,
        ResumeSummary resumeSummary
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ResumeSummary(String fileName, String uploadedAt, java.util.List<String> skills, String status) {
        public static ResumeSummary from(ResumeAnalysisResponse resume) {
            if (resume == null) {
                return null;
            }
            return new ResumeSummary(resume.fileName(), resume.uploadedAt(), resume.skills(), resume.status());
        }
    }
}
