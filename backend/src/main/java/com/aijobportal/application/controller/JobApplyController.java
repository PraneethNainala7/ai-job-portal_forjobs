package com.aijobportal.application.controller;

import com.aijobportal.application.dto.ApplicationEnvelope;
import com.aijobportal.application.service.ApplicationService;
import com.aijobportal.config.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class JobApplyController {

    private final ApplicationService applicationService;

    public JobApplyController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/{id}/apply")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationEnvelope apply(@PathVariable String id) {
        return new ApplicationEnvelope(applicationService.apply(SecurityUtils.requireUser(), id));
    }
}
