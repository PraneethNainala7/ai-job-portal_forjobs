package com.aijobportal.application.service;

import com.aijobportal.ai.dto.MatchResultResponse;
import com.aijobportal.ai.matching.MatchAnalysisService;
import com.aijobportal.application.entity.JobApplication;
import com.aijobportal.application.repository.JobApplicationRepository;
import com.aijobportal.auth.entity.User;
import com.aijobportal.auth.repository.UserRepository;
import com.aijobportal.candidate.entity.Resume;
import com.aijobportal.candidate.repository.CandidateProfileRepository;
import com.aijobportal.candidate.repository.ResumeRepository;
import com.aijobportal.common.domain.AccountStatus;
import com.aijobportal.common.domain.ApplicationStatus;
import com.aijobportal.common.domain.JobStatus;
import com.aijobportal.common.domain.ResumeStatus;
import com.aijobportal.common.domain.Role;
import com.aijobportal.common.exception.ApiException;
import com.aijobportal.config.security.AuthPrincipal;
import com.aijobportal.job.entity.Job;
import com.aijobportal.job.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @TempDir
    Path tempDir;

    @Mock
    private JobApplicationRepository applicationRepository;
    @Mock
    private JobRepository jobRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ResumeRepository resumeRepository;
    @Mock
    private CandidateProfileRepository profileRepository;
    @Mock
    private MatchAnalysisService matchAnalysisService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ApplicationService service;
    private AuthPrincipal candidatePrincipal;
    private AuthPrincipal employerPrincipal;
    private User candidate;
    private User employer;
    private Job job;

    @BeforeEach
    void setUp() {
        service = new ApplicationService(
                applicationRepository,
                jobRepository,
                userRepository,
                resumeRepository,
                profileRepository,
                matchAnalysisService,
                tempDir.toString(),
                eventPublisher
        );
        candidate = user("candidate-1", Role.CANDIDATE);
        employer = user("employer-1", Role.EMPLOYER);
        candidatePrincipal = new AuthPrincipal(candidate.getId(), candidate.getEmail(), candidate.getName(), Role.CANDIDATE, AccountStatus.ACTIVE);
        employerPrincipal = new AuthPrincipal(employer.getId(), employer.getEmail(), employer.getName(), Role.EMPLOYER, AccountStatus.ACTIVE);
        job = activeJob("job-1", employer);
    }

    @Test
    void applyCopiesResumeAndStoresSnapshot() throws Exception {
        Path source = tempDir.resolve("candidate-resume.pdf");
        Files.writeString(source, "resume bytes");

        Resume resume = completeResume(source);
        when(jobRepository.findDetailedById("job-1")).thenReturn(Optional.of(job));
        when(applicationRepository.existsByJobIdAndCandidateId("job-1", candidate.getId())).thenReturn(false);
        when(userRepository.findById(candidate.getId())).thenReturn(Optional.of(candidate));
        when(resumeRepository.findByCandidateId(candidate.getId())).thenReturn(Optional.of(resume));
        when(profileRepository.findByUserId(candidate.getId())).thenReturn(Optional.empty());
        when(matchAnalysisService.analyze(eq(job), any(), eq(resume))).thenReturn(
                new MatchResultResponse("job-1", candidate.getId(), 82, List.of("Java"), List.of("Kubernetes"), "Strong fit")
        );
        when(applicationRepository.save(any())).thenAnswer(invocation -> {
            JobApplication app = invocation.getArgument(0);
            if (app.getAppliedAt() == null) {
                app.setAppliedAt(Instant.now());
            }
            return app;
        });

        service.apply(candidatePrincipal, "job-1");

        ArgumentCaptor<JobApplication> captor = ArgumentCaptor.forClass(JobApplication.class);
        verify(applicationRepository).save(captor.capture());
        JobApplication saved = captor.getValue();
        assertNotNull(saved.getResumeFileUrl());
        assertTrue(Files.isRegularFile(Path.of(saved.getResumeFileUrl())));
        assertEquals("resume.pdf", saved.getResumeFileName());
        assertEquals(Map.of("skills", List.of("Java")), saved.getResumeParsedData());
        assertEquals(82, saved.getMatchScore());
        assertEquals(List.of("Java"), saved.getMatchStrongAreas());
    }

    @Test
    void applyRejectsWhenResumeAnalysisIncomplete() {
        Resume resume = new Resume();
        resume.setCandidate(candidate);
        resume.setStatus(ResumeStatus.PROCESSING);
        resume.setFileUrl(tempDir.resolve("resume.pdf").toString());
        resume.setParsedData(Map.of("skills", List.of("Java")));

        when(jobRepository.findDetailedById("job-1")).thenReturn(Optional.of(job));
        when(applicationRepository.existsByJobIdAndCandidateId("job-1", candidate.getId())).thenReturn(false);
        when(userRepository.findById(candidate.getId())).thenReturn(Optional.of(candidate));
        when(resumeRepository.findByCandidateId(candidate.getId())).thenReturn(Optional.of(resume));

        ApiException ex = assertThrows(ApiException.class, () -> service.apply(candidatePrincipal, "job-1"));
        assertEquals(400, ex.getStatus().value());
        verify(applicationRepository, never()).save(any());
        verify(matchAnalysisService, never()).analyze(any(), any(), any());
    }

    @Test
    void applyAlwaysScoresWithDeterministicMatcher() throws Exception {
        Path source = tempDir.resolve("candidate-resume.pdf");
        Files.writeString(source, "resume bytes");
        Resume resume = completeResume(source);

        when(jobRepository.findDetailedById("job-1")).thenReturn(Optional.of(job));
        when(applicationRepository.existsByJobIdAndCandidateId("job-1", candidate.getId())).thenReturn(false);
        when(userRepository.findById(candidate.getId())).thenReturn(Optional.of(candidate));
        when(resumeRepository.findByCandidateId(candidate.getId())).thenReturn(Optional.of(resume));
        when(profileRepository.findByUserId(candidate.getId())).thenReturn(Optional.empty());
        when(matchAnalysisService.analyze(eq(job), any(), eq(resume))).thenReturn(
                new MatchResultResponse("job-1", candidate.getId(), 82, List.of("Java"), List.of(), "Fit")
        );
        when(applicationRepository.save(any())).thenAnswer(invocation -> {
            JobApplication app = invocation.getArgument(0);
            if (app.getAppliedAt() == null) {
                app.setAppliedAt(Instant.now());
            }
            return app;
        });

        service.apply(candidatePrincipal, "job-1");

        verify(matchAnalysisService).analyze(eq(job), any(), eq(resume));
        verify(applicationRepository).save(any());
    }

    @Test
    void requireResumeDownloadRejectsWrongEmployer() throws Exception {
        JobApplication application = applicationWithResume(employer);
        when(applicationRepository.findDetailedById("app-1")).thenReturn(Optional.of(application));

        AuthPrincipal otherEmployer = new AuthPrincipal("other-employer", "other@test.com", "Other", Role.EMPLOYER, AccountStatus.ACTIVE);
        ApiException ex = assertThrows(ApiException.class, () -> service.requireResumeDownload(otherEmployer, "app-1"));
        assertEquals(404, ex.getStatus().value());
    }

    @Test
    void requireResumeDownloadReturnsFileForOwner() throws Exception {
        Path file = tempDir.resolve("applications/app-1/resume.pdf");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "snapshot");

        JobApplication application = applicationWithResume(employer);
        application.setResumeFileUrl(file.toString());
        application.setResumeFileName("resume.pdf");
        application.setResumeFileType("pdf");
        when(applicationRepository.findDetailedById("app-1")).thenReturn(Optional.of(application));

        var download = service.requireResumeDownload(employerPrincipal, "app-1");
        assertEquals(file, download.path());
        assertEquals("resume.pdf", download.fileName());
        assertEquals("application/pdf", download.mediaType());
    }

    @Test
    void applyRejectsWhenDuplicateDetectedBeforeSave() throws Exception {
        Path source = tempDir.resolve("candidate-resume.pdf");
        Files.writeString(source, "resume bytes");
        Resume resume = completeResume(source);

        when(jobRepository.findDetailedById("job-1")).thenReturn(Optional.of(job));
        when(applicationRepository.existsByJobIdAndCandidateId("job-1", candidate.getId()))
                .thenReturn(false, true);
        when(userRepository.findById(candidate.getId())).thenReturn(Optional.of(candidate));
        when(resumeRepository.findByCandidateId(candidate.getId())).thenReturn(Optional.of(resume));
        when(profileRepository.findByUserId(candidate.getId())).thenReturn(Optional.empty());
        when(matchAnalysisService.analyze(eq(job), any(), eq(resume))).thenReturn(
                new MatchResultResponse("job-1", candidate.getId(), 82, List.of("Java"), List.of(), "Fit")
        );

        ApiException ex = assertThrows(ApiException.class, () -> service.apply(candidatePrincipal, "job-1"));
        assertEquals(400, ex.getStatus().value());
        assertEquals("You have already applied to this job.", ex.getMessage());
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void applyRejectsWhenSaveHitsUniqueConstraint() throws Exception {
        Path source = tempDir.resolve("candidate-resume.pdf");
        Files.writeString(source, "resume bytes");
        Resume resume = completeResume(source);

        when(jobRepository.findDetailedById("job-1")).thenReturn(Optional.of(job));
        when(applicationRepository.existsByJobIdAndCandidateId("job-1", candidate.getId())).thenReturn(false);
        when(userRepository.findById(candidate.getId())).thenReturn(Optional.of(candidate));
        when(resumeRepository.findByCandidateId(candidate.getId())).thenReturn(Optional.of(resume));
        when(profileRepository.findByUserId(candidate.getId())).thenReturn(Optional.empty());
        when(matchAnalysisService.analyze(eq(job), any(), eq(resume))).thenReturn(
                new MatchResultResponse("job-1", candidate.getId(), 82, List.of("Java"), List.of(), "Fit")
        );
        doThrow(new DataIntegrityViolationException("duplicate key (job_id, candidate_id)"))
                .when(applicationRepository).save(any());

        ApiException ex = assertThrows(ApiException.class, () -> service.apply(candidatePrincipal, "job-1"));
        assertEquals(400, ex.getStatus().value());
        assertEquals("You have already applied to this job.", ex.getMessage());
    }

    @Test
    void applyRejectsWhenEmployerOnHold() {
        employer.setAccountStatus(AccountStatus.ON_HOLD);
        job.setEmployer(employer);
        when(jobRepository.findDetailedById("job-1")).thenReturn(Optional.of(job));

        ApiException ex = assertThrows(ApiException.class, () -> service.apply(candidatePrincipal, "job-1"));
        assertEquals(400, ex.getStatus().value());
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void shortlistRejectsWhenApplicationRejected() throws Exception {
        JobApplication application = ownedApplication(ApplicationStatus.REJECTED);
        when(applicationRepository.findDetailedById("app-1")).thenReturn(Optional.of(application));

        ApiException ex = assertThrows(ApiException.class, () -> service.shortlist(employerPrincipal, "app-1"));
        assertEquals(400, ex.getStatus().value());
    }

    @Test
    void rejectRejectsWhenApplicationAlreadyRejected() throws Exception {
        JobApplication application = ownedApplication(ApplicationStatus.REJECTED);
        when(applicationRepository.findDetailedById("app-1")).thenReturn(Optional.of(application));

        ApiException ex = assertThrows(ApiException.class, () -> service.reject(employerPrincipal, "app-1", "reason"));
        assertEquals(400, ex.getStatus().value());
    }

    private JobApplication ownedApplication(ApplicationStatus status) throws Exception {
        JobApplication application = applicationWithResume(employer);
        application.setStatus(status);
        application.setAppliedAt(Instant.now());
        return application;
    }

    private Resume completeResume(Path source) {
        Resume resume = new Resume();
        resume.setCandidate(candidate);
        resume.setStatus(ResumeStatus.COMPLETE);
        resume.setFileUrl(source.toString());
        resume.setFileName("resume.pdf");
        resume.setFileType("pdf");
        resume.setParsedData(Map.of("skills", List.of("Java")));
        return resume;
    }

    private JobApplication applicationWithResume(User owner) throws Exception {
        JobApplication application = new JobApplication();
        application.setId("app-1");
        application.setJob(activeJob("job-1", owner));
        application.setCandidate(candidate);
        Path file = tempDir.resolve("applications/app-1/resume.pdf");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "snapshot");
        application.setResumeFileUrl(file.toString());
        application.setResumeFileName("resume.pdf");
        return application;
    }

    private static User user(String id, Role role) {
        User user = new User();
        user.setId(id);
        user.setName("Test User");
        user.setEmail(id + "@test.com");
        user.setRole(role);
        user.setAccountStatus(AccountStatus.ACTIVE);
        return user;
    }

    private static Job activeJob(String id, User employer) {
        Job job = new Job();
        job.setId(id);
        job.setEmployer(employer);
        job.setStatus(JobStatus.ACTIVE);
        job.setRole("Engineer");
        job.setCompanyName("Acme");
        job.setExperience("3+ years");
        job.setSkills(List.of("Java"));
        job.setLocation("Remote");
        job.setSalary("100k");
        job.setJobType("Full-time");
        job.setDescription("Build things");
        return job;
    }
}
