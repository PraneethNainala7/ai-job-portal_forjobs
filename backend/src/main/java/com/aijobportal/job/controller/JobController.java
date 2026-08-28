package com.aijobportal.job.controller;

import com.aijobportal.ai.service.AiService;
import com.aijobportal.config.security.SecurityUtils;
import com.aijobportal.job.dto.JobListResponse;
import com.aijobportal.job.dto.JobResponse;
import com.aijobportal.job.mapper.JobMapper;
import com.aijobportal.job.service.JobCommandService;
import com.aijobportal.job.service.JobQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobQueryService jobQueryService;
    private final JobCommandService jobCommandService;
    private final AiService aiService;

    public JobController(JobQueryService jobQueryService, JobCommandService jobCommandService, AiService aiService) {
        this.jobQueryService = jobQueryService;
        this.jobCommandService = jobCommandService;
        this.aiService = aiService;
    }

    @GetMapping
    public JobListResponse list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String experience,
            @RequestParam(required = false) String salary,
            @RequestParam(required = false) String jobType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int pageSize
    ) {
        JobListResponse result = jobQueryService.search(search, role, skills, location, experience, salary, jobType, page, pageSize);
        var scored = aiService.attachMatchScores(SecurityUtils.currentUserOrNull(), result.items());
        return new JobListResponse(scored, result.total(), result.page(), result.pageSize());
    }

    @GetMapping("/{id}")
    public JobResponse get(@PathVariable String id) {
        return JobMapper.toJob(jobCommandService.requireJob(id));
    }
}
