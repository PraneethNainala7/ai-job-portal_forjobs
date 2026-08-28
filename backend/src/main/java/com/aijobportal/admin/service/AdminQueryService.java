package com.aijobportal.admin.service;

import com.aijobportal.admin.dto.AdminApplicationResponse;
import com.aijobportal.admin.dto.AdminCandidateDetailResponse;
import com.aijobportal.admin.dto.AdminCandidateListItem;
import com.aijobportal.admin.dto.AdminOverviewResponse;
import com.aijobportal.admin.dto.AdminStatsResponse;
import com.aijobportal.ai.service.AiUsageService;
import com.aijobportal.application.entity.JobApplication;
import com.aijobportal.application.repository.JobApplicationRepository;
import com.aijobportal.audit.entity.AuditLog;
import com.aijobportal.audit.service.AuditService;
import com.aijobportal.auth.dto.UserResponse;
import com.aijobportal.auth.entity.User;
import com.aijobportal.auth.mapper.UserMapper;
import com.aijobportal.auth.repository.UserRepository;
import com.aijobportal.candidate.entity.CandidateProfile;
import com.aijobportal.candidate.entity.Resume;
import com.aijobportal.candidate.mapper.CandidateMapper;
import com.aijobportal.candidate.repository.CandidateProfileRepository;
import com.aijobportal.candidate.repository.ResumeRepository;
import com.aijobportal.common.domain.AccountStatus;
import com.aijobportal.common.domain.JobStatus;
import com.aijobportal.common.domain.Role;
import com.aijobportal.common.exception.ApiException;
import com.aijobportal.employer.repository.CompanyRepository;
import com.aijobportal.job.dto.JobResponse;
import com.aijobportal.job.repository.JobRepository;
import com.aijobportal.job.service.JobQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class AdminQueryService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CandidateProfileRepository profileRepository;
    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;
    private final JobQueryService jobQueryService;
    private final AuditService auditService;
    private final AiUsageService aiUsageService;

    public AdminQueryService(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            CandidateProfileRepository profileRepository,
            ResumeRepository resumeRepository,
            JobRepository jobRepository,
            JobApplicationRepository applicationRepository,
            JobQueryService jobQueryService,
            AuditService auditService,
            AiUsageService aiUsageService
    ) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.profileRepository = profileRepository;
        this.resumeRepository = resumeRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.jobQueryService = jobQueryService;
        this.auditService = auditService;
        this.aiUsageService = aiUsageService;
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse stats() {
        return new AdminStatsResponse(
                userRepository.countByRole(Role.CANDIDATE),
                userRepository.countByRole(Role.EMPLOYER),
                userRepository.count(),
                jobRepository.count(),
                jobRepository.countByStatus(JobStatus.ACTIVE),
                jobRepository.countByStatus(JobStatus.CLOSED),
                applicationRepository.count(),
                userRepository.countByRoleAndAccountStatus(Role.EMPLOYER, AccountStatus.PENDING)
        );
    }

    @Transactional(readOnly = true)
    public AdminOverviewResponse overview() {
        List<JobResponse> jobs = jobQueryService.listAdminJobs();
        List<AdminApplicationResponse> applications = listApplications();
        List<AuditLog> audit = auditService.list();
        return new AdminOverviewResponse(
                stats(),
                jobs.stream().limit(5).toList(),
                applications.stream().limit(5).toList(),
                audit.stream().limit(5).toList(),
                aiUsageService.snapshot()
        );
    }

    @Transactional(readOnly = true)
    public List<AdminCandidateListItem> listCandidates() {
        return userRepository.findByRoleOrderByCreatedAtDesc(Role.CANDIDATE).stream()
                .map(user -> {
                    UserResponse account = UserMapper.toAdminAccount(user, null);
                    CandidateProfile profile = profileRepository.findByUserId(user.getId()).orElse(null);
                    return new AdminCandidateListItem(
                            account.id(),
                            account.name(),
                            account.email(),
                            account.role(),
                            account.accountStatus(),
                            account.createdAt(),
                            account.holdReason(),
                            profile == null ? null : profile.getTitle(),
                            profile == null ? null : profile.getLocation(),
                            profile == null ? null : profile.getExperience()
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminCandidateDetailResponse candidateDetail(String id) {
        User user = requireRole(id, Role.CANDIDATE, "Candidate not found.");
        CandidateProfile profile = profileRepository.findByUserId(id).orElse(null);
        Resume resume = resumeRepository.findByCandidateId(id).orElse(null);
        return new AdminCandidateDetailResponse(
                UserMapper.toAdminAccount(user, null),
                profile == null ? null : CandidateMapper.toProfile(profile),
                AdminCandidateDetailResponse.ResumeSummary.from(resume == null ? null : CandidateMapper.toResume(resume))
        );
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listEmployers() {
        return userRepository.findByRoleOrderByCreatedAtDesc(Role.EMPLOYER).stream()
                .map(user -> UserMapper.toAdminAccount(user, companyRepository.findByEmployerId(user.getId()).orElse(null)))
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse employerDetail(String id) {
        User user = requireRole(id, Role.EMPLOYER, "Employer not found.");
        return UserMapper.toAdminAccount(user, companyRepository.findByEmployerId(id).orElse(null));
    }

    @Transactional(readOnly = true)
    public List<AdminApplicationResponse> listApplications() {
        return applicationRepository.findAllFetched().stream()
                .sorted(Comparator.comparing(JobApplication::getAppliedAt).reversed())
                .map(this::toAdminApplication)
                .toList();
    }

    public User requireRole(String id, Role role, String missing) {
        User user = userRepository.findById(id).orElseThrow(() -> ApiException.notFound(missing));
        if (user.getRole() != role) {
            throw ApiException.notFound(missing);
        }
        return user;
    }

    private AdminApplicationResponse toAdminApplication(JobApplication application) {
        String employerName = application.getJob().getEmployer() == null ? null : application.getJob().getEmployer().getName();
        return new AdminApplicationResponse(
                application.getId(),
                application.getJob().getRole(),
                application.getJob().getCompanyName(),
                employerName,
                application.getCandidate().getName(),
                application.getStatus().name(),
                application.getAppliedAt().toString()
        );
    }
}
