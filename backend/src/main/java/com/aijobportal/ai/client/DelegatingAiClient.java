package com.aijobportal.ai.client;

import com.aijobportal.ai.dto.InterviewQuestionsResponse;
import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.candidate.dto.ResumeAnalysisResponse;
import com.aijobportal.candidate.mapper.CandidateMapper;
import com.aijobportal.common.domain.ResumeStatus;
import com.aijobportal.job.entity.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DelegatingAiClient implements AiClient {

    private static final Logger log = LoggerFactory.getLogger(DelegatingAiClient.class);

    private final AiClient primary;
    private final HeuristicAiClient interviewQuestions;

    public DelegatingAiClient(AiClient primary, HeuristicAiClient interviewQuestions) {
        this.primary = primary;
        this.interviewQuestions = interviewQuestions;
    }

    @Override
    public ResumeAnalysisOutcome analyzeResume(
            CandidateProfileResponse profile,
            ResumeAnalysisResponse current,
            String resumeText
    ) {
        try {
            return primary.analyzeResume(profile, current, resumeText);
        } catch (RuntimeException ex) {
            log.warn("Claude resume analysis failed: {}", ex.getMessage());
            return DisabledAiClient.failedOutcome(
                    current,
                    "Resume analysis failed: " + ex.getMessage()
                            + ". Check ANTHROPIC_API_KEY on the Spring Boot process and retry."
            );
        }
    }

    @Override
    public InterviewQuestionsResponse interviewQuestions(Job job, CandidateProfileResponse candidate) {
        return interviewQuestions.interviewQuestions(job, candidate);
    }
}
