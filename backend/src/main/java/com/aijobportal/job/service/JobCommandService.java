package com.aijobportal.job.service;

import com.aijobportal.auth.entity.User;
import com.aijobportal.auth.repository.UserRepository;
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

import java.util.List;

@Service
public class JobCommandService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final JobQueryService jobQueryService;

    public JobCommandService(
            JobRepository jobRepository,
            CompanyRepository companyRepository,
            UserRepository userRepository,
            JobQueryService jobQueryService
    ) {
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.jobQueryService = jobQueryService;
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
        job.setRole(input.role().trim());
        job.setExperience(input.experience().trim());
        job.setSkills(Lists.copy(input.skills()));
        List<String> preferred = input.preferredSkills() == null
                ? null
                : input.preferredSkills().stream().map(String::trim).filter(value -> !value.isBlank()).toList();
        job.setPreferredSkills(preferred == null || preferred.isEmpty() ? null : preferred);
        job.setLocation(input.location().trim());
        job.setSalary(input.salary().trim());
        job.setJobType(input.jobType().trim());
        job.setWorkMode(blankToNull(input.workMode()));
        job.setDescription(input.description().trim());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
