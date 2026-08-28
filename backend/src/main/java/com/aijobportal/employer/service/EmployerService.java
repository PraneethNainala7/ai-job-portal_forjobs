package com.aijobportal.employer.service;

import com.aijobportal.application.entity.JobApplication;
import com.aijobportal.application.mapper.ApplicationSnapshotMapper;
import com.aijobportal.application.repository.JobApplicationRepository;
import com.aijobportal.application.service.ApplicationService;
import com.aijobportal.ai.dto.MatchResultResponse;
import com.aijobportal.auth.dto.UserResponse;
import com.aijobportal.auth.entity.User;
import com.aijobportal.auth.mapper.UserMapper;
import com.aijobportal.auth.repository.UserRepository;
import com.aijobportal.auth.service.AccountStatusRules;
import com.aijobportal.candidate.entity.CandidateProfile;
import com.aijobportal.candidate.repository.CandidateProfileRepository;
import com.aijobportal.common.domain.AccountStatus;
import com.aijobportal.common.domain.ApplicationStatus;
import com.aijobportal.common.domain.JobStatus;
import com.aijobportal.common.exception.ApiException;
import com.aijobportal.config.security.AuthPrincipal;
import com.aijobportal.employer.dto.CompanyUpdateRequest;
import com.aijobportal.employer.dto.EmployerApplicantResponse;
import com.aijobportal.employer.dto.EmployerDashboardResponse;
import com.aijobportal.employer.entity.Company;
import com.aijobportal.employer.repository.CompanyRepository;
import com.aijobportal.job.entity.Job;
import com.aijobportal.job.repository.JobRepository;
import com.aijobportal.job.service.JobCommandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class EmployerService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;
    private final CandidateProfileRepository profileRepository;
    private final JobCommandService jobCommandService;
    private final ApplicationService applicationService;

    public EmployerService(
            CompanyRepository companyRepository,
            UserRepository userRepository,
            JobRepository jobRepository,
            JobApplicationRepository applicationRepository,
            CandidateProfileRepository profileRepository,
            JobCommandService jobCommandService,
            ApplicationService applicationService
    ) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.profileRepository = profileRepository;
        this.jobCommandService = jobCommandService;
        this.applicationService = applicationService;
    }

    @Transactional(readOnly = true)
    public UserResponse profile(AuthPrincipal principal) {
        AccountStatusRules.requireEmployer(principal.role());
        User user = userRepository.findById(principal.id()).orElseThrow(() -> ApiException.unauthorized("Sign-in required."));
        return UserMapper.toUser(user, companyRepository.findByEmployerId(user.getId()).orElse(null));
    }

    @Transactional
    public UserResponse updateProfile(AuthPrincipal principal, CompanyUpdateRequest request) {
        AccountStatusRules.requireEmployer(principal.role());
        if (blank(request.companyName()) || blank(request.companyInformation()) || blank(request.companyLocation())) {
            throw ApiException.badRequest("Company name, information, and location are required.");
        }
        User user = userRepository.findById(principal.id()).orElseThrow(() -> ApiException.unauthorized("Sign-in required."));
        Company company = companyRepository.findByEmployerId(user.getId())
                .orElseThrow(() -> ApiException.badRequest("Company profile could not be updated."));
        company.setCompanyName(request.companyName().trim());
        company.setDescription(request.companyInformation().trim());
        company.setLocation(request.companyLocation().trim());
        company.setWebsite(blank(request.companyWebsite()) ? null : request.companyWebsite().trim());
        return UserMapper.toUser(user, company);
    }

    @Transactional
    public UserResponse resubmit(AuthPrincipal principal) {
        AccountStatusRules.requireEmployer(principal.role());
        User user = userRepository.findById(principal.id()).orElseThrow(() -> ApiException.unauthorized("Sign-in required."));
        if (user.getAccountStatus() != AccountStatus.REJECTED) {
            throw ApiException.badRequest("Only a rejected registration can be resubmitted.");
        }
        user.setAccountStatus(AccountStatus.PENDING);
        user.setStatusReason(null);
        user.setStatusChangedAt(Instant.now());
        return UserMapper.toUser(user, companyRepository.findByEmployerId(user.getId()).orElse(null));
    }

    @Transactional(readOnly = true)
    public EmployerDashboardResponse dashboard(AuthPrincipal principal) {
        AccountStatusRules.requireActiveEmployer(principal.role(), principal.accountStatus());
        List<Job> jobs = jobRepository.findByEmployerIdOrderByCreatedAtDesc(principal.id());
        List<JobApplication> applicants = jobs.stream()
                .flatMap(job -> applicationRepository.findByJobIdOrderByAppliedAtDesc(job.getId()).stream())
                .toList();
        return new EmployerDashboardResponse(
                jobs.size(),
                (int) jobs.stream().filter(job -> job.getStatus() == JobStatus.ACTIVE).count(),
                applicants.size(),
                (int) applicants.stream().filter(item -> item.getStatus() == ApplicationStatus.SHORTLISTED).count(),
                (int) applicants.stream().filter(item -> item.getStatus() == ApplicationStatus.INTERVIEW).count()
        );
    }

    @Transactional(readOnly = true)
    public List<EmployerApplicantResponse> applicants(AuthPrincipal principal, String jobId) {
        AccountStatusRules.requireActiveEmployer(principal.role(), principal.accountStatus());
        Job job = jobCommandService.requireOwned(jobId, principal.id());
        return applicationService.listForJob(jobId).stream()
                .map(this::toApplicant)
                .toList();
    }

    private EmployerApplicantResponse toApplicant(JobApplication application) {
        CandidateProfile profile = profileRepository.findByUserId(application.getCandidate().getId()).orElse(null);
        var candidate = ApplicationSnapshotMapper.candidateFromApplication(application, profile);
        MatchResultResponse match = ApplicationSnapshotMapper.matchFromApplication(application);
        return new EmployerApplicantResponse(
                applicationService.toResponse(application, false),
                candidate,
                match,
                ApplicationSnapshotMapper.resumeInfo(application)
        );
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
