package com.aijobportal.ai.client;

import com.aijobportal.ai.dto.InterviewQuestionsResponse;
import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.candidate.dto.ResumeAnalysisResponse;
import com.aijobportal.candidate.mapper.CandidateMapper;
import com.aijobportal.common.domain.ResumeStatus;
import com.aijobportal.job.entity.Job;

import java.util.List;

/**
 * Used when Claude is not configured. Resume analysis always fails with a clear message.
 */
public class DisabledAiClient implements AiClient {

    static final String MISSING_API_KEY_MESSAGE =
            "AI resume analysis is not configured. Set ANTHROPIC_API_KEY on the Spring Boot process and retry.";

    private final HeuristicAiClient interviewQuestions;

    public DisabledAiClient(HeuristicAiClient interviewQuestions) {
        this.interviewQuestions = interviewQuestions;
    }

    @Override
    public ResumeAnalysisOutcome analyzeResume(
            CandidateProfileResponse profile,
            ResumeAnalysisResponse current,
            String resumeText
    ) {
        return failedOutcome(current, MISSING_API_KEY_MESSAGE);
    }

    @Override
    public InterviewQuestionsResponse interviewQuestions(Job job, CandidateProfileResponse candidate) {
        return interviewQuestions.interviewQuestions(job, candidate);
    }

    public static ResumeAnalysisOutcome failedOutcome(ResumeAnalysisResponse current, String error) {
        ResumeAnalysisResponse response = new ResumeAnalysisResponse(
                ResumeStatus.FAILED.name(),
                current.fileName(),
                current.uploadedAt(),
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
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                error
        );
        return new ResumeAnalysisOutcome(response, CandidateMapper.toParsedData(response));
    }
}
