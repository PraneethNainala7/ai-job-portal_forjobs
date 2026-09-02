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
        if (critical.isEmpty() && required.isEmpty()) {
            throw ApiException.badRequest("Add at least one required skill.");
        }
        validateSkillTiers(critical, required, preferred);

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
        job.setEducationRequirements(normalizeTier(input.educationRequirements()));
        job.setCertificationRequirements(normalizeTier(input.certificationRequirements()));
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

    private void validateSkillTiers(List<String> critical, List<String> required, List<String> preferred) {
        assertNoOverlap(required, preferred, "required", "preferred");
        assertNoOverlap(critical, preferred, "critical", "preferred");
        assertSubset(critical, required);
    }

    private void assertSubset(List<String> subset, List<String> superset) {
        if (subset.isEmpty() || superset.isEmpty()) {
            return;
        }
        Set<String> allowed = new LinkedHashSet<>();
        for (String skill : superset) {
            allowed.add(normalizationService.normalizeKey(skill));
        }
        for (String skill : subset) {
            String key = normalizationService.normalizeKey(skill);
            if (!allowed.contains(key)) {
                throw ApiException.badRequest("Critical skill '" + skill
                        + "' must also appear in required skills.");
            }
        }
    }

    private void assertNoOverlap(List<String> left, List<String> right, String leftName, String rightName) {
        if (left.isEmpty() || right.isEmpty()) {
            return;
        }
        Set<String> seen = new LinkedHashSet<>();
        for (String skill : left) {
            seen.add(normalizationService.normalizeKey(skill));
        }
        for (String skill : right) {
            String key = normalizationService.normalizeKey(skill);
            if (seen.contains(key)) {
                throw ApiException.badRequest("Skill '" + skill + "' appears in both "
                        + leftName + " and " + rightName + " tiers.");
            }
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
