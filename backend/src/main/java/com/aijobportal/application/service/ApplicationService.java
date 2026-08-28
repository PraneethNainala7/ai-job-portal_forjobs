package com.aijobportal.application.service;

import com.aijobportal.ai.client.AiClient;
import com.aijobportal.ai.dto.MatchResultResponse;
import com.aijobportal.application.dto.ApplicationResponse;
import com.aijobportal.application.dto.ApplicationResumeDownload;
import com.aijobportal.application.entity.JobApplication;
import com.aijobportal.application.repository.JobApplicationRepository;
import com.aijobportal.auth.entity.User;
import com.aijobportal.auth.repository.UserRepository;
import com.aijobportal.auth.service.AccountStatusRules;
import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.candidate.entity.CandidateProfile;
import com.aijobportal.candidate.entity.Resume;
import com.aijobportal.candidate.mapper.CandidateMapper;
import com.aijobportal.candidate.repository.CandidateProfileRepository;
import com.aijobportal.candidate.repository.ResumeRepository;
import com.aijobportal.common.domain.ApplicationStatus;
import com.aijobportal.common.domain.JobStatus;
import com.aijobportal.common.domain.ResumeStatus;
import com.aijobportal.common.exception.ApiException;
import com.aijobportal.common.util.Ids;
import com.aijobportal.config.security.AuthPrincipal;
import com.aijobportal.job.entity.Job;
import com.aijobportal.job.mapper.JobMapper;
import com.aijobportal.job.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);
    private static final String ALREADY_APPLIED_MESSAGE = "You have already applied to this job.";

    private final JobApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final CandidateProfileRepository profileRepository;
    private final AiClient aiClient;
    private final Path uploadDir;

    public ApplicationService(
            JobApplicationRepository applicationRepository,
            JobRepository jobRepository,
            UserRepository userRepository,
            ResumeRepository resumeRepository,
            CandidateProfileRepository profileRepository,
            AiClient aiClient,
            @Value("${app.upload-dir}") String uploadDir
    ) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.resumeRepository = resumeRepository;
        this.profileRepository = profileRepository;
        this.aiClient = aiClient;
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @Transactional
    public ApplicationResponse apply(AuthPrincipal principal, String jobId) {
        AccountStatusRules.requireCandidateActive(principal.role(), principal.accountStatus());
        Job job = jobRepository.findById(jobId).orElseThrow(() -> ApiException.badRequest("This job is not open."));
        if (job.getStatus() != JobStatus.ACTIVE) {
            throw ApiException.badRequest("This job is not open.");
        }
        assertNotAlreadyApplied(jobId, principal.id());

        User candidate = userRepository.findById(principal.id())
                .orElseThrow(() -> ApiException.unauthorized("Sign-in required."));
        Resume resume = resumeRepository.findByCandidateId(principal.id())
                .orElseThrow(() -> ApiException.badRequest("Upload and analyze your resume before applying."));
        if (resume.getStatus() != ResumeStatus.COMPLETE) {
            throw ApiException.badRequest("Complete resume analysis before applying.");
        }
        if (resume.getFileUrl() == null || resume.getFileUrl().isBlank()) {
            throw ApiException.badRequest("Upload and analyze your resume before applying.");
        }
        Path source = Path.of(resume.getFileUrl()).normalize();
        if (!Files.isRegularFile(source)) {
            throw ApiException.badRequest("Resume file is missing. Re-upload your resume.");
        }
        if (resume.getParsedData() == null || resume.getParsedData().isEmpty()) {
            throw ApiException.badRequest("Complete resume analysis before applying.");
        }

        CandidateProfile profile = profileRepository.findByUserId(principal.id()).orElse(null);
        CandidateProfileResponse scoringProfile = scoringProfile(candidate, profile, resume);

        List<MatchResultResponse> ranked;
        try {
            ranked = aiClient.rankJobs(scoringProfile, List.of(job));
        } catch (RuntimeException ex) {
            log.warn("Apply-time match scoring failed for candidate {} job {}: {}", principal.id(), jobId, ex.getMessage());
            throw ApiException.badRequest("Match scoring is unavailable. Try again later.");
        }
        if (ranked.isEmpty()) {
            throw ApiException.badRequest("Match scoring is unavailable. Try again later.");
        }
        MatchResultResponse match = ranked.getFirst();

        JobApplication application = new JobApplication();
        application.setId(Ids.next());
        application.setJob(job);
        application.setCandidate(candidate);
        application.setStatus(ApplicationStatus.APPLIED);

        String fileName = resume.getFileName() == null ? "resume.pdf" : resume.getFileName();
        String sanitized = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        try {
            Path targetDir = uploadDir.resolve("applications").resolve(application.getId());
            Files.createDirectories(targetDir);
            Path target = targetDir.resolve(sanitized).normalize();
            if (!target.startsWith(uploadDir)) {
                throw ApiException.badRequest("Could not store the application resume.");
            }
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            application.setResumeFileUrl(target.toString());
            application.setResumeFileName(fileName);
            application.setResumeFileType(resume.getFileType());
        } catch (IOException ex) {
            log.error("Failed to copy resume for application {}", application.getId(), ex);
            throw ApiException.badRequest("Could not store the application resume.");
        }

        application.setResumeParsedData(copyParsedData(resume.getParsedData()));
        application.setMatchScore(match.matchScore());
        application.setMatchStrongAreas(match.strongAreas());
        application.setMatchGaps(match.gaps());
        application.setMatchExplanation(match.explanation());

        assertNotAlreadyApplied(jobId, principal.id());
        try {
            applicationRepository.save(application);
        } catch (DataIntegrityViolationException ex) {
            throw ApiException.badRequest(ALREADY_APPLIED_MESSAGE);
        }
        return toResponse(application, false);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> listForCandidate(AuthPrincipal principal) {
        AccountStatusRules.requireCandidateActive(principal.role(), principal.accountStatus());
        return applicationRepository.findByCandidateIdOrderByAppliedAtDesc(principal.id()).stream()
                .map(item -> toResponse(item, true))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<JobApplication> listForJob(String jobId) {
        return applicationRepository.findByJobIdOrderByAppliedAtDesc(jobId);
    }

    @Transactional
    public ApplicationResponse shortlist(AuthPrincipal principal, String applicationId) {
        JobApplication application = requireOwnedApplication(principal, applicationId);
        ApplicationStatusRules.requireCanShortlist(application.getStatus());
        application.setStatus(ApplicationStatus.SHORTLISTED);
        return toResponse(application, false);
    }

    @Transactional
    public ApplicationResponse reject(AuthPrincipal principal, String applicationId, String reason) {
        JobApplication application = requireOwnedApplication(principal, applicationId);
        ApplicationStatusRules.requireCanReject(application.getStatus());
        application.setStatus(ApplicationStatus.REJECTED);
        application.setRejectionReason(reason == null || reason.isBlank() ? null : reason.trim());
        return toResponse(application, false);
    }

    @Transactional(readOnly = true)
    public ApplicationResumeDownload requireResumeDownload(AuthPrincipal principal, String applicationId) {
        JobApplication application = requireOwnedApplication(principal, applicationId);
        if (application.getResumeFileUrl() == null || application.getResumeFileUrl().isBlank()) {
            throw ApiException.notFound("Resume not found.");
        }
        Path path = Path.of(application.getResumeFileUrl()).normalize();
        if (!Files.isRegularFile(path)) {
            throw ApiException.notFound("Resume not found.");
        }
        String fileName = application.getResumeFileName() == null ? "resume.pdf" : application.getResumeFileName();
        return new ApplicationResumeDownload(path, fileName, mediaType(application.getResumeFileType(), fileName));
    }

    @Transactional
    public JobApplication requireOwnedApplication(AuthPrincipal principal, String applicationId) {
        AccountStatusRules.requireActiveEmployer(principal.role(), principal.accountStatus());
        JobApplication application = applicationRepository.findDetailedById(applicationId)
                .orElseThrow(() -> ApiException.notFound("Application not found."));
        if (application.getJob().getEmployer() == null || !principal.id().equals(application.getJob().getEmployer().getId())) {
            throw ApiException.notFound("Application not found.");
        }
        return application;
    }

    public ApplicationResponse toResponse(JobApplication application, boolean includeJob) {
        return new ApplicationResponse(
                application.getId(),
                application.getJob().getId(),
                application.getCandidate().getId(),
                application.getStatus().name(),
                application.getAppliedAt().toString(),
                application.getRejectionReason(),
                includeJob ? JobMapper.toJob(application.getJob()) : null
        );
    }

    private void assertNotAlreadyApplied(String jobId, String candidateId) {
        if (applicationRepository.existsByJobIdAndCandidateId(jobId, candidateId)) {
            throw ApiException.badRequest(ALREADY_APPLIED_MESSAGE);
        }
    }

    private static CandidateProfileResponse scoringProfile(User candidate, CandidateProfile profile, Resume resume) {
        CandidateProfileResponse base = profile == null
                ? new CandidateProfileResponse(
                        candidate.getId(), candidate.getName(), candidate.getEmail(),
                        null, null, null, null, List.of(), List.of(), List.of(), null, null)
                : CandidateMapper.toProfile(profile);
        return CandidateMapper.withResumeSkills(base, resume);
    }

    private static Map<String, Object> copyParsedData(Map<String, Object> parsedData) {
        return new LinkedHashMap<>(parsedData);
    }

    private static String mediaType(String fileType, String fileName) {
        if (fileType != null) {
            return switch (fileType.toLowerCase(Locale.ROOT)) {
                case "pdf" -> "application/pdf";
                case "doc" -> "application/msword";
                case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                default -> "application/octet-stream";
            };
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".pdf")) {
            return "application/pdf";
        }
        if (lower.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        if (lower.endsWith(".doc")) {
            return "application/msword";
        }
        return "application/octet-stream";
    }
}
