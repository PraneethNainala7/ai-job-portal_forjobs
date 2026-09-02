package com.aijobportal.job.service;

import com.aijobportal.ai.matching.normalization.SkillNormalizationService;
import com.aijobportal.auth.entity.User;
import com.aijobportal.auth.repository.UserRepository;
import com.aijobportal.common.domain.AccountStatus;
import com.aijobportal.common.domain.JobStatus;
import com.aijobportal.common.exception.ApiException;
import com.aijobportal.common.util.Lists;
import com.aijobportal.config.security.AuthPrincipal;
import com.aijobportal.employer.entity.Company;
import com.aijobportal.employer.repository.CompanyRepository;
import com.aijobportal.job.dto.JobInputRequest;
import com.aijobportal.job.dto.JobResponse;
import com.aijobportal.job.entity.Job;
import com.aijobportal.job.mapper.JobMapper;
import com.aijobportal.job.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class JobCommandService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final JobQueryService jobQueryService;
    private final SkillNormalizationService normalizationService;

    public JobCommandService(
            JobRepository jobRepository,
            CompanyRepository companyRepository,
            UserRepository userRepository,
            JobQueryService jobQueryService,
            SkillNormalizationService normalizationService
    ) {
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.jobQueryService = jobQueryService;
        this.normalizationService = normalizationService;
    }

    @Transactional(readOnly = true)
    public Job requireOwned(String jobId, String employerId) {
        return jobRepository.findByIdAndEmployerId(jobId, employerId)
                .orElseThrow(() -> ApiException.notFound("Job not found."));
    }

    @Transactional(readOnly = true)
    public Job requireJob(String id) {
        return jobRepository.findById(id).orElseThrow(() -> ApiException.notFound("Job not found."));
    }

    @Transactional(readOnly = true)
    public Job requirePublicJob(String id) {
        Job job = jobRepository.findDetailedById(id).orElseThrow(() -> ApiException.notFound("Job not found."));
        if (job.getStatus() != JobStatus.ACTIVE
                || job.getEmployer() == null
                || job.getEmployer().getAccountStatus() != AccountStatus.ACTIVE) {
            throw ApiException.notFound("Job not found.");
        }
        return job;
    }

    @Transactional
    public JobResponse create(AuthPrincipal principal, JobInputRequest input) {
        User employer = userRepository.findById(principal.id())
                .orElseThrow(() -> ApiException.unauthorized("Sign-in required."));
        Company company = companyRepository.findByEmployerId(principal.id())
                .orElseThrow(() -> ApiException.badRequest("Complete the company profile before posting jobs."));
        Job job = new Job();
        job.setEmployer(employer);
        job.setCompanyName(company.getCompanyName());
        apply(job, input);
        job.setStatus(JobStatus.ACTIVE);
        jobRepository.save(job);
        return JobMapper.toJob(job);
    }

    @Transactional
    public JobResponse update(AuthPrincipal principal, String id, JobInputRequest input) {
        Job job = requireOwned(id, principal.id());
        if (job.getStatus() == JobStatus.CLOSED) {
            throw ApiException.badRequest("Closed jobs cannot be edited.");
        }
        apply(job, input);
        return JobMapper.toJob(job);
    }

    @Transactional
    public JobResponse closeOwned(AuthPrincipal principal, String id) {
        Job job = requireOwned(id, principal.id());
        job.setStatus(JobStatus.CLOSED);
        return JobMapper.toJob(job);
    }

    @Transactional
    public JobResponse adminClose(String id) {
        Job job = requireJob(id);
        job.setStatus(JobStatus.CLOSED);
        return jobQueryService.toAdminJob(job);
    }

    @Transactional
    public void adminDelete(String id) {
        if (!jobRepository.existsById(id)) {
            throw ApiException.notFound("Job not found.");
        }
        jobRepository.deleteById(id);
    }

    private void apply(Job job, JobInputRequest input) {
        List<String> critical = normalizeTier(input.criticalSkills());
        List<String> required = normalizeTier(input.skills());
        List<String> preferred = normalizeTier(input.preferredSkills());
        if (critical.isEmpty()) {
            throw ApiException.badRequest("Add at least one critical skill.");
        }
        validateNoOverlap(critical, required, preferred);

        job.setRole(input.role().trim());
        job.setExperience(input.experience().trim());
        job.setCriticalSkills(critical);
        job.setSkills(required);
        job.setPreferredSkills(preferred.isEmpty() ? null : preferred);
        job.setLocation(input.location().trim());
        job.setSalary(input.salary().trim());
        job.setJobType(input.jobType().trim());
        job.setWorkMode(blankToNull(input.workMode()));
        job.setDescription(input.description().trim());
    }

    private List<String> normalizeTier(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        List<String> normalized = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            String trimmed = value.trim();
            String key = normalizationService.normalizeKey(trimmed);
            if (!key.isBlank() && seen.add(key)) {
                normalized.add(trimmed);
            }
        }
        return normalized;
    }

    private void validateNoOverlap(List<String> critical, List<String> required, List<String> preferred) {
        Set<String> seen = new LinkedHashSet<>();
        assertUniqueTier(seen, critical, "critical");
        assertUniqueTier(seen, required, "required");
        assertUniqueTier(seen, preferred, "preferred");
    }

    private void assertUniqueTier(Set<String> seen, List<String> tier, String tierName) {
        for (String skill : tier) {
            String key = normalizationService.normalizeKey(skill);
            if (!seen.add(key)) {
                throw ApiException.badRequest("Skill '" + skill + "' appears in more than one tier. "
                        + "Each skill must belong to only one of critical or preferred.");
            }
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
