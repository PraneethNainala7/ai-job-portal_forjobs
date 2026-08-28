package com.aijobportal.employer.controller;

import com.aijobportal.application.dto.ApplicationResponse;
import com.aijobportal.application.dto.ReasonRequest;
import com.aijobportal.application.service.ApplicationService;
import com.aijobportal.auth.dto.UserResponse;
import com.aijobportal.auth.repository.UserRepository;
import com.aijobportal.auth.service.AccountStatusRules;
import com.aijobportal.config.security.AuthCookieService;
import com.aijobportal.config.security.SecurityUtils;
import com.aijobportal.employer.dto.CompanyUpdateRequest;
import com.aijobportal.employer.dto.EmployerApplicantItemsResponse;
import com.aijobportal.employer.dto.EmployerDashboardResponse;
import com.aijobportal.employer.repository.CompanyRepository;
import com.aijobportal.employer.service.EmployerService;
import com.aijobportal.job.dto.JobInputRequest;
import com.aijobportal.job.dto.JobItemsResponse;
import com.aijobportal.job.dto.JobResponse;
import com.aijobportal.job.service.JobCommandService;
import com.aijobportal.job.service.JobQueryService;
import com.aijobportal.application.dto.ApplicationResumeDownload;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employer")
public class EmployerController {

    private final EmployerService employerService;
    private final JobQueryService jobQueryService;
    private final JobCommandService jobCommandService;
    private final ApplicationService applicationService;
    private final AuthCookieService authCookieService;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    public EmployerController(
            EmployerService employerService,
            JobQueryService jobQueryService,
            JobCommandService jobCommandService,
            ApplicationService applicationService,
            AuthCookieService authCookieService,
            UserRepository userRepository,
            CompanyRepository companyRepository
    ) {
        this.employerService = employerService;
        this.jobQueryService = jobQueryService;
        this.jobCommandService = jobCommandService;
        this.applicationService = applicationService;
        this.authCookieService = authCookieService;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    @GetMapping("/profile")
    public UserResponse profile() {
        return employerService.profile(SecurityUtils.requireUser());
    }

    @PutMapping("/profile")
    public UserResponse updateProfile(@RequestBody CompanyUpdateRequest request, HttpServletResponse response) {
        UserResponse updated = employerService.updateProfile(SecurityUtils.requireUser(), request);
        refreshCookie(response, updated.id());
        return updated;
    }

    @PostMapping("/resubmit")
    public UserResponse resubmit(HttpServletResponse response) {
        UserResponse updated = employerService.resubmit(SecurityUtils.requireUser());
        refreshCookie(response, updated.id());
        return updated;
    }

    @GetMapping("/dashboard")
    public EmployerDashboardResponse dashboard() {
        return employerService.dashboard(SecurityUtils.requireUser());
    }

    @GetMapping("/jobs")
    public JobItemsResponse jobs() {
        var principal = SecurityUtils.requireUser();
        AccountStatusRules.requireActiveEmployer(principal.role(), principal.accountStatus());
        return new JobItemsResponse(jobQueryService.listEmployerJobs(principal.id()));
    }

    @PostMapping("/jobs")
    @ResponseStatus(HttpStatus.CREATED)
    public JobResponse createJob(@Valid @RequestBody JobInputRequest request) {
        var principal = SecurityUtils.requireUser();
        AccountStatusRules.requireActiveEmployer(principal.role(), principal.accountStatus());
        return jobCommandService.create(principal, request);
    }

    @GetMapping("/jobs/{id}")
    public JobResponse getJob(@PathVariable String id) {
        var principal = SecurityUtils.requireUser();
        AccountStatusRules.requireActiveEmployer(principal.role(), principal.accountStatus());
        return jobQueryService.toEmployerJob(jobCommandService.requireOwned(id, principal.id()));
    }

    @PutMapping("/jobs/{id}")
    public JobResponse updateJob(@PathVariable String id, @Valid @RequestBody JobInputRequest request) {
        var principal = SecurityUtils.requireUser();
        AccountStatusRules.requireActiveEmployer(principal.role(), principal.accountStatus());
        return jobCommandService.update(principal, id, request);
    }

    @PostMapping("/jobs/{id}/close")
    public JobResponse closeJob(@PathVariable String id) {
        var principal = SecurityUtils.requireUser();
        AccountStatusRules.requireActiveEmployer(principal.role(), principal.accountStatus());
        return jobCommandService.closeOwned(principal, id);
    }

    @GetMapping("/jobs/{id}/applicants")
    public EmployerApplicantItemsResponse applicants(@PathVariable String id) {
        return new EmployerApplicantItemsResponse(employerService.applicants(SecurityUtils.requireUser(), id));
    }

    @PutMapping("/applications/{id}/shortlist")
    public ApplicationResponse shortlist(@PathVariable String id) {
        return applicationService.shortlist(SecurityUtils.requireUser(), id);
    }

    @PutMapping("/applications/{id}/reject")
    public ApplicationResponse reject(@PathVariable String id, @RequestBody(required = false) ReasonRequest body) {
        String reason = body == null ? null : body.reason();
        return applicationService.reject(SecurityUtils.requireUser(), id, reason);
    }

    @GetMapping("/applications/{id}/resume")
    public ResponseEntity<UrlResource> downloadResume(@PathVariable String id) {
        ApplicationResumeDownload download = applicationService.requireResumeDownload(SecurityUtils.requireUser(), id);
        try {
            UrlResource resource = new UrlResource(download.path().toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(download.mediaType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + download.fileName() + "\"")
                    .body(resource);
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

    private void refreshCookie(HttpServletResponse response, String userId) {
        userRepository.findById(userId).ifPresent(user ->
                authCookieService.setSession(response, user, companyRepository.findByEmployerId(user.getId()).orElse(null)));
    }
}
