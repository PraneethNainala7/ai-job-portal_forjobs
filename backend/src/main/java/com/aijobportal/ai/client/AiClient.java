package com.aijobportal.ai.client;

import com.aijobportal.ai.dto.InterviewQuestionsResponse;
import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.candidate.dto.ResumeAnalysisResponse;
import com.aijobportal.job.entity.Job;

public interface AiClient {

    ResumeAnalysisOutcome analyzeResume(
            CandidateProfileResponse profile,
            ResumeAnalysisResponse current,
            String resumeText
    );

    InterviewQuestionsResponse interviewQuestions(Job job, CandidateProfileResponse candidate);
}
