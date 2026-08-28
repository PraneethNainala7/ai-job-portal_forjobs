package com.aijobportal.admin.controller;

import com.aijobportal.admin.dto.AdminApplicationResponse;
import com.aijobportal.admin.service.AdminQueryService;
import com.aijobportal.ai.service.AiUsageService;
import com.aijobportal.audit.dto.AuditListResponse;
import com.aijobportal.audit.service.AuditService;
import com.aijobportal.auth.dto.OkResponse;
import com.aijobportal.common.response.ItemsResponse;
import com.aijobportal.config.security.SecurityUtils;
import com.aijobportal.job.dto.JobResponse;
import com.aijobportal.job.service.JobCommandService;
import com.aijobportal.job.service.JobQueryService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminOpsController {

    private final AdminQueryService adminQueryService;
    private final JobQueryService jobQueryService;
    private final JobCommandService jobCommandService;
    private final AuditService auditService;
    private final AiUsageService aiUsageService;

    public AdminOpsController(
            AdminQueryService adminQueryService,
            JobQueryService jobQueryService,
            JobCommandService jobCommandService,
            AuditService auditService,
            AiUsageService aiUsageService
    ) {
        this.adminQueryService = adminQueryService;
        this.jobQueryService = jobQueryService;
        this.jobCommandService = jobCommandService;
        this.auditService = auditService;
        this.aiUsageService = aiUsageService;
    }

    @GetMapping("/jobs/{id}")
    public JobResponse job(@PathVariable String id) {
        return jobQueryService.getAdminJob(id);
    }

    @PutMapping("/jobs/{id}/close")
    public JobResponse closeJob(@PathVariable String id) {
        var admin = SecurityUtils.requireUser();
        JobResponse before = jobQueryService.getAdminJob(id);
        JobResponse closed = jobCommandService.adminClose(id);
        auditService.record(admin, "CLOSE_JOB", "JOB", id, before.status(), "CLOSED", null);
        return closed;
    }

    @DeleteMapping("/jobs/{id}")
    public OkResponse deleteJob(@PathVariable String id) {
        var admin = SecurityUtils.requireUser();
        JobResponse before = jobQueryService.getAdminJob(id);
        jobCommandService.adminDelete(id);
        auditService.record(admin, "DELETE_JOB", "JOB", id, before.status(), null, null);
        return OkResponse.yes();
    }

    @GetMapping("/applications")
    public ItemsResponse<AdminApplicationResponse> applications() {
        return new ItemsResponse<>(adminQueryService.listApplications());
    }

    @GetMapping("/audit")
    public AuditListResponse audit() {
        return new AuditListResponse(auditService.list(), aiUsageService.snapshot());
    }
}
