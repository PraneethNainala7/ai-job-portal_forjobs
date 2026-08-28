package com.aijobportal.ai.client;

import com.aijobportal.ai.dto.InterviewQuestionsResponse;
import com.aijobportal.ai.dto.MatchResultResponse;
import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.candidate.dto.ResumeAnalysisResponse;
import com.aijobportal.job.entity.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DelegatingAiClient implements AiClient {

    private static final Logger log = LoggerFactory.getLogger(DelegatingAiClient.class);

    private final AiClient primary;
    private final HeuristicAiClient fallback;

    public DelegatingAiClient(AiClient primary, HeuristicAiClient fallback) {
        this.primary = primary;
        this.fallback = fallback;
    }

    @Override
    public ResumeAnalysisResponse analyzeResume(
            CandidateProfileResponse profile,
            ResumeAnalysisResponse current,
            String resumeText
    ) {
        try {
            return primary.analyzeResume(profile, current, resumeText);
        } catch (RuntimeException ex) {
            log.warn("Claude resume analysis failed; using heuristic. {}", ex.getMessage());
            return fallback.analyzeResume(profile, current, resumeText);
        }
    }

    @Override
    public MatchResultResponse scoreJob(Job job, CandidateProfileResponse profile) {
        return fallback.scoreJob(job, profile);
    }

    @Override
    public List<MatchResultResponse> rankJobs(CandidateProfileResponse profile, List<Job> jobs) {
        try {
            return primary.rankJobs(profile, jobs);
        } catch (RuntimeException ex) {
            log.warn("Claude job ranking failed; not using heuristic scores. {}", ex.getMessage());
            return List.of();
        }
    }

    @Override
    public InterviewQuestionsResponse interviewQuestions(Job job, CandidateProfileResponse candidate) {
        return fallback.interviewQuestions(job, candidate);
    }
}
