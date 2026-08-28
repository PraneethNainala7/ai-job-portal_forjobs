package com.aijobportal.employer.dto;

import com.aijobportal.application.dto.ApplicationResponse;
import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.ai.dto.MatchResultResponse;

public record EmployerApplicantResponse(
        ApplicationResponse application,
        CandidateProfileResponse candidate,
        MatchResultResponse match,
        ApplicationResumeInfo resume
) {
}
