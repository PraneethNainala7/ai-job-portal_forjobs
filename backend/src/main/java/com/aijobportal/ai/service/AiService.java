package com.aijobportal.ai.service;

import com.aijobportal.ai.client.AiClient;
import com.aijobportal.ai.client.ResumeTextExtractor;
import com.aijobportal.ai.dto.InterviewQuestionsEnvelope;
import com.aijobportal.ai.dto.InterviewQuestionsRequest;
import com.aijobportal.ai.dto.InterviewQuestionsResponse;
import com.aijobportal.ai.dto.MatchResultResponse;
import com.aijobportal.ai.dto.RecommendationItem;
import com.aijobportal.ai.dto.RecommendationResponse;
import com.aijobportal.ai.entity.InterviewQuestionSet;
import com.aijobportal.ai.entity.MatchResultEntity;
import com.aijobportal.ai.repository.InterviewQuestionRepository;
import com.aijobportal.ai.repository.MatchResultRepository;
import com.aijobportal.application.entity.JobApplication;
import com.aijobportal.application.repository.JobApplicationRepository;
import com.aijobportal.auth.service.AccountStatusRules;
import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.candidate.dto.ResumeAnalysisResponse;
import com.aijobportal.candidate.entity.Resume;
import com.aijobportal.candidate.mapper.CandidateMapper;
import com.aijobportal.candidate.repository.ResumeRepository;
import com.aijobportal.candidate.service.CandidateProfileService;
import com.aijobportal.common.domain.AccountStatus;
import com.aijobportal.common.domain.ApplicationStatus;
import com.aijobportal.common.domain.ResumeStatus;
import com.aijobportal.common.domain.Role;
import com.aijobportal.common.exception.ApiException;
import com.aijobportal.config.security.AuthPrincipal;
import com.aijobportal.job.dto.JobListResponse;
import com.aijobportal.job.dto.JobResponse;
import com.aijobportal.job.entity.Job;
import com.aijobportal.job.mapper.JobMapper;
import com.aijobportal.job.service.JobCommandService;
import com.aijobportal.job.service.JobQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final AiClient aiClient;
    private final ResumeTextExtractor resumeTextExtractor;
    private final AiUsageService aiUsageService;
    private final CandidateProfileService candidateProfileService;
    private final ResumeRepository resumeRepository;
    private final JobQueryService jobQueryService;
    private final JobCommandService jobCommandService;
    private final MatchResultRepository matchResultRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final JobApplicationRepository applicationRepository;

    public AiService(
            AiClient aiClient,
            ResumeTextExtractor resumeTextExtractor,
            AiUsageService aiUsageService,
            CandidateProfileService candidateProfileService,
            ResumeRepository resumeRepository,
            JobQueryService jobQueryService,
            JobCommandService jobCommandService,
            MatchResultRepository matchResultRepository,
            InterviewQuestionRepository interviewQuestionRepository,
            JobApplicationRepository applicationRepository
    ) {
        this.aiClient = aiClient;
        this.resumeTextExtractor = resumeTextExtractor;
        this.aiUsageService = aiUsageService;
        this.candidateProfileService = candidateProfileService;
        this.resumeRepository = resumeRepository;
        this.jobQueryService = jobQueryService;
        this.jobCommandService = jobCommandService;
        this.matchResultRepository = matchResultRepository;
        this.interviewQuestionRepository = interviewQuestionRepository;
        this.applicationRepository = applicationRepository;
    }

    @Transactional
    public ResumeAnalysisResponse analyzeResume(AuthPrincipal principal) {
        CandidateProfileResponse profile = candidateProfileService.getProfile(principal);
        Resume resume = resumeRepository.findByCandidateId(principal.id())
                .orElseThrow(() -> ApiException.badRequest("Upload a resume before running analysis."));
        String resumeText = resumeTextExtractor.extractOrEmpty(resume.getFileUrl(), resume.getFileName());
        resume.setParsedText(resumeText.isBlank() ? null : resumeText);
        ResumeAnalysisResponse result = aiClient.analyzeResume(profile, CandidateMapper.toResume(resume), resumeText);
        resume.setStatus(ResumeStatus.valueOf(result.status()));
        resume.setError(result.error());
        resume.setParsedData(CandidateMapper.toParsedData(result));
        if (result.status().equals(ResumeStatus.FAILED.name())) {
            aiUsageService.increment("failures");
        } else {
            aiUsageService.increment("resumeAnalysis");
        }
        return result;
    }

    @Transactional
    public RecommendationResponse recommend(AuthPrincipal principal) {
        CandidateProfileResponse profile = candidateProfileService.scoringProfile(principal);
        JobListResponse jobs = jobQueryService.search(null, null, null, null, null, null, null, 1, 20);
        List<Job> entities = jobs.items().stream().map(job -> jobCommandService.requireJob(job.id())).toList();
        Map<String, JobResponse> byId = new LinkedHashMap<>();
        jobs.items().forEach(job -> byId.put(job.id(), job));
        List<MatchResultResponse> ranked;
        try {
            ranked = aiClient.rankJobs(profile, entities);
        } catch (RuntimeException ex) {
            log.warn("Claude job ranking failed; returning no scores. {}", ex.getMessage());
            return new RecommendationResponse(List.of());
        }
        if (ranked.isEmpty()) {
            return new RecommendationResponse(List.of());
        }
        List<RecommendationItem> items = ranked.stream()
                .filter(match -> byId.get(match.jobId()) != null)
                .map(match -> {
                    MatchResultResponse stored = persistMatch(match);
                    return new RecommendationItem(
                            JobMapper.withMatch(byId.get(match.jobId()), stored.matchScore()),
                            stored
                    );
                })
                .sorted(Comparator.comparingInt((RecommendationItem item) -> item.match().matchScore()).reversed())
                .limit(6)
                .toList();
        aiUsageService.increment("recommendations");
        return new RecommendationResponse(items);
    }

    @Transactional
    public MatchResultResponse match(AuthPrincipal principal, String jobId) {
        CandidateProfileResponse profile = candidateProfileService.scoringProfile(principal);
        Job job = jobCommandService.requireJob(jobId);
        List<MatchResultResponse> ranked;
        try {
            ranked = aiClient.rankJobs(profile, List.of(job));
        } catch (RuntimeException ex) {
            log.warn("Claude match failed; not using heuristic scores. {}", ex.getMessage());
            throw ApiException.unavailable("Match analysis is unavailable for this role.");
        }
        if (ranked.isEmpty()) {
            throw ApiException.unavailable("Match analysis is unavailable for this role.");
        }
        MatchResultResponse match = persistMatch(ranked.getFirst());
        aiUsageService.increment("jobMatch");
        return match;
    }

    @Transactional(readOnly = true)
    public List<JobResponse> attachMatchScores(AuthPrincipal principal, List<JobResponse> jobs) {
        if (principal == null || principal.role() != Role.CANDIDATE || principal.accountStatus() != AccountStatus.ACTIVE) {
            return jobs;
        }
        if (jobs == null || jobs.isEmpty()) {
            return jobs;
        }
        Resume resume = resumeRepository.findByCandidateId(principal.id()).orElse(null);
        if (resume == null || resume.getStatus() != ResumeStatus.COMPLETE) {
            return jobs;
        }
        CandidateProfileResponse profile = candidateProfileService.scoringProfile(principal);
        List<Job> entities = jobs.stream().map(job -> jobCommandService.requireJob(job.id())).toList();
        List<MatchResultResponse> ranked;
        try {
            ranked = aiClient.rankJobs(profile, entities);
        } catch (RuntimeException ex) {
            log.warn("Claude job list scoring failed; returning jobs without scores. {}", ex.getMessage());
            return jobs;
        }
        if (ranked.isEmpty()) {
            return jobs;
        }
        Map<String, Integer> scoresByJobId = ranked.stream()
                .collect(Collectors.toMap(MatchResultResponse::jobId, MatchResultResponse::matchScore, (left, right) -> left));
        return jobs.stream()
                .map(job -> {
                    Integer score = scoresByJobId.get(job.id());
                    return score == null ? job : JobMapper.withMatch(job, score);
                })
                .toList();
    }

    @Transactional
    public InterviewQuestionsEnvelope interviewQuestions(AuthPrincipal principal, InterviewQuestionsRequest request) {
        AccountStatusRules.requireActiveEmployer(principal.role(), principal.accountStatus());
        if (request.jobId() == null || request.candidateId() == null) {
            throw ApiException.badRequest("Job and candidate are required.");
        }
        Job job = jobCommandService.requireOwned(request.jobId(), principal.id());
        JobApplication application = applicationRepository.findByJobIdAndCandidateId(request.jobId(), request.candidateId())
                .orElseThrow(() -> ApiException.notFound("Application not found."));
        CandidateProfileResponse candidate = candidateProfileService.getProfileByUserId(request.candidateId());
        if (application.getStatus() == ApplicationStatus.APPLIED || application.getStatus() == ApplicationStatus.SHORTLISTED) {
            application.setStatus(ApplicationStatus.INTERVIEW);
        }
        InterviewQuestionsResponse questions = aiClient.interviewQuestions(job, candidate);
        InterviewQuestionSet stored = new InterviewQuestionSet();
        stored.setJobId(job.getId());
        stored.setCandidateId(request.candidateId());
        stored.setQuestions(toMap(questions));
        interviewQuestionRepository.save(stored);
        aiUsageService.increment("interviewQuestions");
        return new InterviewQuestionsEnvelope(
                questions,
                "These questions are AI-generated decision support. They are not a hiring decision."
        );
    }

    private MatchResultResponse persistMatch(MatchResultResponse match) {
        MatchResultEntity entity = new MatchResultEntity();
        entity.setJobId(match.jobId());
        entity.setCandidateId(match.candidateId());
        entity.setMatchScore(match.matchScore());
        entity.setStrongAreas(match.strongAreas());
        entity.setGaps(match.gaps());
        entity.setExplanation(match.explanation());
        matchResultRepository.save(entity);
        return match;
    }

    private static Map<String, Object> toMap(InterviewQuestionsResponse questions) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("technical", questions.technical());
        map.put("resumeBased", questions.resumeBased());
        map.put("jobSpecific", questions.jobSpecific());
        map.put("behavioral", questions.behavioral());
        return map;
    }
}
