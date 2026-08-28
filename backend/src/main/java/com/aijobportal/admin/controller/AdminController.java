package com.aijobportal.admin.controller;

import com.aijobportal.admin.dto.AdminApplicationResponse;
import com.aijobportal.admin.dto.AdminCandidateDetailResponse;
import com.aijobportal.admin.dto.AdminCandidateListItem;
import com.aijobportal.admin.dto.AdminOverviewResponse;
import com.aijobportal.admin.dto.AdminStatsResponse;
import com.aijobportal.admin.service.AdminAccountService;
import com.aijobportal.admin.service.AdminQueryService;
import com.aijobportal.application.dto.ReasonRequest;
import com.aijobportal.auth.dto.OkResponse;
import com.aijobportal.auth.dto.UserResponse;
import com.aijobportal.common.domain.AccountStatus;
import com.aijobportal.common.domain.Role;
import com.aijobportal.common.response.ItemsResponse;
import com.aijobportal.config.security.SecurityUtils;
import com.aijobportal.job.dto.JobResponse;
import com.aijobportal.job.service.JobQueryService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminQueryService adminQueryService;
    private final AdminAccountService adminAccountService;
    private final JobQueryService jobQueryService;

    public AdminController(
            AdminQueryService adminQueryService,
            AdminAccountService adminAccountService,
            JobQueryService jobQueryService
    ) {
        this.adminQueryService = adminQueryService;
        this.adminAccountService = adminAccountService;
        this.jobQueryService = jobQueryService;
    }

    @GetMapping("/dashboard")
    public AdminOverviewResponse dashboard() {
        return adminQueryService.overview();
    }

    @GetMapping("/statistics")
    public AdminStatsResponse statistics() {
        return adminQueryService.stats();
    }

    @GetMapping("/candidates")
    public ItemsResponse<AdminCandidateListItem> candidates() {
        return new ItemsResponse<>(adminQueryService.listCandidates());
    }

    @GetMapping("/candidates/{id}")
    public AdminCandidateDetailResponse candidate(@PathVariable String id) {
        return adminQueryService.candidateDetail(id);
    }

    @GetMapping("/employers")
    public ItemsResponse<UserResponse> employers() {
        return new ItemsResponse<>(adminQueryService.listEmployers());
    }

    @GetMapping("/employers/pending")
    public ItemsResponse<UserResponse> pendingEmployers() {
        return new ItemsResponse<>(adminQueryService.listEmployers().stream()
                .filter(item -> AccountStatus.PENDING.name().equals(item.accountStatus()))
                .toList());
    }

    @GetMapping("/employers/{id}")
    public UserResponse employer(@PathVariable String id) {
        return adminQueryService.employerDetail(id);
    }

    @PutMapping("/employers/{id}/approve")
    public UserResponse approve(@PathVariable String id) {
        adminQueryService.requireRole(id, Role.EMPLOYER, "Employer not found.");
        return adminAccountService.changeStatus(SecurityUtils.requireUser(), id, AccountStatus.ACTIVE, "APPROVE_EMPLOYER", null);
    }

    @PutMapping("/employers/{id}/reject")
    public UserResponse reject(@PathVariable String id, @RequestBody(required = false) ReasonRequest body) {
        String reason = body == null ? null : body.reason();
        if (reason == null || reason.isBlank()) {
            throw com.aijobportal.common.exception.ApiException.badRequest("A rejection reason is required.");
        }
        return adminAccountService.changeStatus(SecurityUtils.requireUser(), id, AccountStatus.REJECTED, "REJECT_EMPLOYER", reason.trim());
    }

    @PutMapping("/employers/{id}/hold")
    public UserResponse holdEmployer(@PathVariable String id, @RequestBody(required = false) ReasonRequest body) {
        String reason = body == null || body.reason() == null || body.reason().isBlank() ? null : body.reason().trim();
        return adminAccountService.changeStatus(SecurityUtils.requireUser(), id, AccountStatus.ON_HOLD, "HOLD_EMPLOYER", reason);
    }

    @PutMapping("/employers/{id}/activate")
    public UserResponse activateEmployer(@PathVariable String id) {
        return adminAccountService.changeStatus(SecurityUtils.requireUser(), id, AccountStatus.ACTIVE, "ACTIVATE_EMPLOYER", null);
    }

    @PutMapping("/employers/{id}/resubmit")
    public UserResponse resubmit(@PathVariable String id) {
        return adminAccountService.resubmitEmployer(SecurityUtils.requireUser(), id);
    }

    @PutMapping("/users/{id}/hold")
    public UserResponse holdUser(@PathVariable String id, @RequestBody(required = false) ReasonRequest body) {
        String reason = body == null || body.reason() == null || body.reason().isBlank() ? null : body.reason().trim();
        return adminAccountService.changeStatus(SecurityUtils.requireUser(), id, AccountStatus.ON_HOLD, "HOLD_USER", reason);
    }

    @PutMapping("/users/{id}/activate")
    public UserResponse activateUser(@PathVariable String id) {
        return adminAccountService.changeStatus(SecurityUtils.requireUser(), id, AccountStatus.ACTIVE, "ACTIVATE_USER", null);
    }

    @PutMapping("/users/{id}/deactivate")
    public UserResponse deactivateUser(@PathVariable String id) {
        return adminAccountService.changeStatus(SecurityUtils.requireUser(), id, AccountStatus.INACTIVE, "DEACTIVATE_USER", null);
    }

    @DeleteMapping("/users/{id}")
    public OkResponse deleteUser(@PathVariable String id) {
        adminAccountService.deleteUser(SecurityUtils.requireUser(), id);
        return OkResponse.yes();
    }

    @GetMapping("/jobs")
    public ItemsResponse<JobResponse> jobs() {
        return new ItemsResponse<>(jobQueryService.listAdminJobs());
    }
}
