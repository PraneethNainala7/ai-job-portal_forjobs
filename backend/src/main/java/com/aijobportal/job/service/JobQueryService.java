package com.aijobportal.job.service;

import com.aijobportal.application.repository.JobApplicationRepository;
import com.aijobportal.common.domain.ApplicationStatus;
import com.aijobportal.common.domain.JobStatus;
import com.aijobportal.job.dto.JobListResponse;
import com.aijobportal.job.dto.JobResponse;
import com.aijobportal.job.entity.Job;
import com.aijobportal.job.mapper.JobMapper;
import com.aijobportal.job.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class JobQueryService {

    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;

    public JobQueryService(JobRepository jobRepository, JobApplicationRepository jobApplicationRepository) {
        this.jobRepository = jobRepository;
        this.jobApplicationRepository = jobApplicationRepository;
    }

    @Transactional(readOnly = true)
    public JobListResponse search(
            String search,
            String role,
            String skills,
            String location,
            String experience,
            String salary,
            String jobType,
            int page,
            int pageSize
    ) {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(20, Math.max(1, pageSize <= 0 ? 6 : pageSize));
        List<Job> filtered = jobRepository.findByStatusOrderByPostedDateDesc(JobStatus.ACTIVE).stream()
                .filter(job -> matches(job, search, role, skills, location, experience, salary, jobType))
                .toList();
        int start = (safePage - 1) * safeSize;
        List<JobResponse> items = filtered.stream().skip(start).limit(safeSize).map(JobMapper::toJob).toList();
        return new JobListResponse(items, filtered.size(), safePage, safeSize);
    }

    @Transactional(readOnly = true)
    public List<JobResponse> listEmployerJobs(String employerId) {
        return jobRepository.findByEmployerIdOrderByCreatedAtDesc(employerId).stream()
                .map(this::toEmployerJob)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<JobResponse> listAdminJobs() {
        return jobRepository.findAllWithEmployer().stream().map(this::toAdminJob).toList();
    }

    @Transactional(readOnly = true)
    public JobResponse getAdminJob(String id) {
        Job job = jobRepository.findDetailedById(id)
                .orElseThrow(() -> com.aijobportal.common.exception.ApiException.notFound("Job not found."));
        return toAdminJob(job);
    }

    public JobResponse toEmployerJob(Job job) {
        long applicants = jobApplicationRepository.countByJobId(job.getId());
        long shortlisted = jobApplicationRepository.countByJobIdAndStatusIn(
                job.getId(),
                List.of(ApplicationStatus.SHORTLISTED, ApplicationStatus.INTERVIEW)
        );
        return JobMapper.toJob(job, null, (int) applicants, (int) shortlisted, null);
    }

    public JobResponse toAdminJob(Job job) {
        String employerName = job.getEmployer() == null ? null : job.getEmployer().getName();
        long applicants = jobApplicationRepository.countByJobId(job.getId());
        return JobMapper.toJob(job, null, (int) applicants, null, employerName);
    }

    private boolean matches(
            Job job,
            String search,
            String role,
            String skills,
            String location,
            String experience,
            String salary,
            String jobType
    ) {
        String haystack = (job.getRole() + " " + job.getCompanyName() + " " + job.getDescription() + " "
                + String.join(" ", job.getSkills())).toLowerCase(Locale.ROOT);
        if (has(search) && !haystack.contains(search.toLowerCase(Locale.ROOT))) {
            return false;
        }
        if (has(role) && !job.getRole().toLowerCase(Locale.ROOT).contains(role.toLowerCase(Locale.ROOT))) {
            return false;
        }
        if (has(location) && !job.getLocation().toLowerCase(Locale.ROOT).contains(location.toLowerCase(Locale.ROOT))) {
            return false;
        }
        if (has(experience) && !job.getExperience().toLowerCase(Locale.ROOT).contains(experience.toLowerCase(Locale.ROOT))) {
            return false;
        }
        if (has(jobType) && !job.getJobType().equals(jobType)) {
            return false;
        }
        if (has(salary) && !job.getSalary().toLowerCase(Locale.ROOT).contains(salary.toLowerCase(Locale.ROOT))) {
            return false;
        }
        return !has(skills) || job.getSkills().stream()
                .anyMatch(skill -> skill.toLowerCase(Locale.ROOT).contains(skills.toLowerCase(Locale.ROOT)));
    }

    private static boolean has(String value) {
        return value != null && !value.isBlank();
    }
}
