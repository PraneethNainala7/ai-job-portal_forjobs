package com.aijobportal.ai.service;

import com.aijobportal.ai.client.AiClient;
import com.aijobportal.ai.client.ResumeTextExtractor;
import com.aijobportal.ai.config.AiProperties;
import com.aijobportal.ai.dto.MatchResultResponse;
import com.aijobportal.ai.matching.MatchAnalysisService;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    private MatchAnalysisService matchAnalysisService;
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

    private AiProperties aiProperties;
    private AiService service;
    private AuthPrincipal candidatePrincipal;
    private Job jobEntity;
    private JobResponse jobResponse;
    private Job lowMatchJobEntity;
    private JobResponse lowMatchJobResponse;

    @BeforeEach
    void setUp() {
        aiProperties = new AiProperties();
        service = new AiService(
                aiClient,
                matchAnalysisService,
                resumeTextExtractor,
                aiUsageService,
                candidateProfileService,
                resumeRepository,
                jobQueryService,
                jobCommandService,
                matchResultRepository,
                interviewQuestionRepository,
                applicationRepository,
                aiProperties
        );
        candidatePrincipal = new AuthPrincipal(
                "candidate-1", "candidate@test.com", "Candidate", Role.CANDIDATE, AccountStatus.ACTIVE);
        jobEntity = job("job-1", "Engineer");
        jobResponse = JobMapper.toJob(jobEntity);
        lowMatchJobEntity = job("job-2", "Designer");
        lowMatchJobResponse = JobMapper.toJob(lowMatchJobEntity);
    }

    @Test
    void attachMatchScoresReturnsUnchangedWhenPrincipalNull() {
        List<JobResponse> jobs = List.of(jobResponse);

        List<JobResponse> result = service.attachMatchScores(null, jobs);

        assertEquals(jobs, result);
        verify(matchAnalysisService, never()).rankAll(any(), any(), any());
    }

    @Test
    void attachMatchScoresReturnsUnchangedForEmployer() {
        AuthPrincipal employer = new AuthPrincipal(
                "employer-1", "employer@test.com", "Employer", Role.EMPLOYER, AccountStatus.ACTIVE);
        List<JobResponse> jobs = List.of(jobResponse);

        List<JobResponse> result = service.attachMatchScores(employer, jobs);

        assertNull(result.getFirst().matchScore());
        verify(matchAnalysisService, never()).rankAll(any(), any(), any());
    }

    @Test
    void attachMatchScoresMergesDeterministicScoresForActiveCandidate() {
        Resume resume = completeResume();
        CandidateProfileResponse profile = profile();

        when(resumeRepository.findByCandidateId("candidate-1")).thenReturn(Optional.of(resume));
        when(candidateProfileService.scoringProfile(candidatePrincipal)).thenReturn(profile);
        when(jobCommandService.requireJob("job-1")).thenReturn(jobEntity);
        when(matchAnalysisService.rankAll(eq(profile), eq(List.of(jobEntity)), eq(resume))).thenReturn(List.of(
                new MatchResultResponse("job-1", "candidate-1", 84, List.of("Java"), List.of(), "Strong fit")
        ));

        List<JobResponse> result = service.attachMatchScores(candidatePrincipal, List.of(jobResponse));

        assertEquals(84, result.getFirst().matchScore());
    }

    @Test
    void attachMatchScoresStillReturnsLowScoresForFindJobs() {
        Resume resume = completeResume();
        CandidateProfileResponse profile = profile();

        when(resumeRepository.findByCandidateId("candidate-1")).thenReturn(Optional.of(resume));
        when(candidateProfileService.scoringProfile(candidatePrincipal)).thenReturn(profile);
        when(jobCommandService.requireJob("job-2")).thenReturn(lowMatchJobEntity);
        when(matchAnalysisService.rankAll(eq(profile), eq(List.of(lowMatchJobEntity)), eq(resume))).thenReturn(List.of(
                new MatchResultResponse("job-2", "candidate-1", 18, List.of(), List.of("Java"), "Limited overlap")
        ));

        List<JobResponse> result = service.attachMatchScores(candidatePrincipal, List.of(lowMatchJobResponse));

        assertEquals(18, result.getFirst().matchScore());
    }

    @Test
    void recommendReturnsEmptyWhenResumeNotComplete() {
        when(resumeRepository.findByCandidateId("candidate-1")).thenReturn(Optional.empty());

        var result = service.recommend(candidatePrincipal);

        assertTrue(result.items().isEmpty());
        assertFalse(result.resumeReady());
        verify(jobQueryService, never()).listAllActive();
    }

    @Test
    void recommendFiltersBelowMinScore() {
        Resume resume = completeResume();
        CandidateProfileResponse profile = profile();

        when(resumeRepository.findByCandidateId("candidate-1")).thenReturn(Optional.of(resume));
        when(jobQueryService.listAllActive()).thenReturn(List.of(jobResponse, lowMatchJobResponse));
        when(jobCommandService.requireJob("job-1")).thenReturn(jobEntity);
        when(jobCommandService.requireJob("job-2")).thenReturn(lowMatchJobEntity);
        when(candidateProfileService.scoringProfile(candidatePrincipal)).thenReturn(profile);
        when(matchAnalysisService.rankAll(eq(profile), eq(List.of(jobEntity, lowMatchJobEntity)), eq(resume))).thenReturn(List.of(
                new MatchResultResponse("job-1", "candidate-1", 70, List.of("Java"), List.of(), "Good fit"),
                new MatchResultResponse("job-2", "candidate-1", 18, List.of(), List.of("Java"), "Limited fit")
        ));

        var result = service.recommend(candidatePrincipal);

        assertEquals(1, result.items().size());
        assertEquals("job-1", result.items().getFirst().job().id());
        assertEquals(70, result.items().getFirst().match().matchScore());
        assertEquals(2, result.evaluatedCount());
        assertEquals(60, result.minScore());
        assertTrue(result.resumeReady());
    }

    @Test
    void recommendReturnsEmptyWhenAllBelowMinScore() {
        Resume resume = completeResume();
        CandidateProfileResponse profile = profile();

        when(resumeRepository.findByCandidateId("candidate-1")).thenReturn(Optional.of(resume));
        when(jobQueryService.listAllActive()).thenReturn(List.of(lowMatchJobResponse));
        when(jobCommandService.requireJob("job-2")).thenReturn(lowMatchJobEntity);
        when(candidateProfileService.scoringProfile(candidatePrincipal)).thenReturn(profile);
        when(matchAnalysisService.rankAll(eq(profile), eq(List.of(lowMatchJobEntity)), eq(resume))).thenReturn(List.of(
                new MatchResultResponse("job-2", "candidate-1", 18, List.of(), List.of("Java"), "Limited fit")
        ));

        var result = service.recommend(candidatePrincipal);

        assertTrue(result.items().isEmpty());
        assertEquals(1, result.evaluatedCount());
        assertTrue(result.resumeReady());
    }

    private static Resume completeResume() {
        Resume resume = new Resume();
        resume.setStatus(ResumeStatus.COMPLETE);
        return resume;
    }

    private static CandidateProfileResponse profile() {
        return new CandidateProfileResponse(
                "candidate-1", "Candidate", "candidate@test.com",
                null, null, null, null, List.of("Java"), List.of(), List.of(), null, null);
    }

    private static Job job(String id, String role) {
        Job job = new Job();
        job.setId(id);
        job.setRole(role);
        job.setCompanyName("Acme");
        job.setExperience("3+ years");
        job.setSkills(List.of("Java"));
        job.setLocation("Remote");
        job.setSalary("100k");
        job.setJobType("Full-time");
        job.setDescription("Build things");
        job.setStatus(JobStatus.ACTIVE);
        return job;
    }
}
