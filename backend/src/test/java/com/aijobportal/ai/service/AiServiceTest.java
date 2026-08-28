package com.aijobportal.ai.service;

import com.aijobportal.ai.client.AiClient;
import com.aijobportal.ai.client.ResumeTextExtractor;
import com.aijobportal.ai.dto.MatchResultResponse;
import com.aijobportal.ai.repository.InterviewQuestionRepository;
import com.aijobportal.ai.repository.MatchResultRepository;
import com.aijobportal.application.repository.JobApplicationRepository;
import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.candidate.entity.Resume;
import com.aijobportal.candidate.repository.ResumeRepository;
import com.aijobportal.candidate.service.CandidateProfileService;
import com.aijobportal.common.domain.AccountStatus;
import com.aijobportal.common.domain.JobStatus;
import com.aijobportal.common.domain.ResumeStatus;
import com.aijobportal.common.domain.Role;
import com.aijobportal.config.security.AuthPrincipal;
import com.aijobportal.job.dto.JobResponse;
import com.aijobportal.job.entity.Job;
import com.aijobportal.job.mapper.JobMapper;
import com.aijobportal.job.service.JobCommandService;
import com.aijobportal.job.service.JobQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private AiClient aiClient;
    @Mock
    private ResumeTextExtractor resumeTextExtractor;
    @Mock
    private AiUsageService aiUsageService;
    @Mock
    private CandidateProfileService candidateProfileService;
    @Mock
    private ResumeRepository resumeRepository;
    @Mock
    private JobQueryService jobQueryService;
    @Mock
    private JobCommandService jobCommandService;
    @Mock
    private MatchResultRepository matchResultRepository;
    @Mock
    private InterviewQuestionRepository interviewQuestionRepository;
    @Mock
    private JobApplicationRepository applicationRepository;

    private AiService service;
    private AuthPrincipal candidatePrincipal;
    private Job jobEntity;
    private JobResponse jobResponse;

    @BeforeEach
    void setUp() {
        service = new AiService(
                aiClient,
                resumeTextExtractor,
                aiUsageService,
                candidateProfileService,
                resumeRepository,
                jobQueryService,
                jobCommandService,
                matchResultRepository,
                interviewQuestionRepository,
                applicationRepository
        );
        candidatePrincipal = new AuthPrincipal(
                "candidate-1", "candidate@test.com", "Candidate", Role.CANDIDATE, AccountStatus.ACTIVE);
        jobEntity = new Job();
        jobEntity.setId("job-1");
        jobEntity.setRole("Engineer");
        jobEntity.setCompanyName("Acme");
        jobEntity.setExperience("3+ years");
        jobEntity.setSkills(List.of("Java"));
        jobEntity.setLocation("Remote");
        jobEntity.setSalary("100k");
        jobEntity.setJobType("Full-time");
        jobEntity.setDescription("Build things");
        jobEntity.setStatus(JobStatus.ACTIVE);
        jobResponse = JobMapper.toJob(jobEntity);
    }

    @Test
    void attachMatchScoresReturnsUnchangedWhenPrincipalNull() {
        List<JobResponse> jobs = List.of(jobResponse);

        List<JobResponse> result = service.attachMatchScores(null, jobs);

        assertEquals(jobs, result);
        verify(aiClient, never()).rankJobs(any(), any());
    }

    @Test
    void attachMatchScoresReturnsUnchangedForEmployer() {
        AuthPrincipal employer = new AuthPrincipal(
                "employer-1", "employer@test.com", "Employer", Role.EMPLOYER, AccountStatus.ACTIVE);
        List<JobResponse> jobs = List.of(jobResponse);

        List<JobResponse> result = service.attachMatchScores(employer, jobs);

        assertNull(result.getFirst().matchScore());
        verify(aiClient, never()).rankJobs(any(), any());
    }

    @Test
    void attachMatchScoresMergesClaudeScoresForActiveCandidate() {
        Resume resume = new Resume();
        resume.setStatus(ResumeStatus.COMPLETE);
        CandidateProfileResponse profile = new CandidateProfileResponse(
                "candidate-1", "Candidate", "candidate@test.com",
                null, null, null, null, List.of("Java"), List.of(), List.of(), null, null);

        when(resumeRepository.findByCandidateId("candidate-1")).thenReturn(Optional.of(resume));
        when(candidateProfileService.scoringProfile(candidatePrincipal)).thenReturn(profile);
        when(jobCommandService.requireJob("job-1")).thenReturn(jobEntity);
        when(aiClient.rankJobs(eq(profile), eq(List.of(jobEntity)))).thenReturn(List.of(
                new MatchResultResponse("job-1", "candidate-1", 84, List.of("Java"), List.of(), "Strong fit")
        ));

        List<JobResponse> result = service.attachMatchScores(candidatePrincipal, List.of(jobResponse));

        assertEquals(84, result.getFirst().matchScore());
    }
}
