package com.aijobportal.ai.client;

import com.aijobportal.ai.dto.InterviewQuestionsResponse;
import com.aijobportal.ai.dto.MatchResultResponse;
import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.candidate.dto.ResumeAnalysisResponse;
import com.aijobportal.job.entity.Job;

import java.util.List;

public interface AiClient {

    ResumeAnalysisResponse analyzeResume(
            CandidateProfileResponse profile,
            ResumeAnalysisResponse current,
            String resumeText
    );

    MatchResultResponse scoreJob(Job job, CandidateProfileResponse profile);

    List<MatchResultResponse> rankJobs(CandidateProfileResponse profile, List<Job> jobs);

    InterviewQuestionsResponse interviewQuestions(Job job, CandidateProfileResponse candidate);
}
