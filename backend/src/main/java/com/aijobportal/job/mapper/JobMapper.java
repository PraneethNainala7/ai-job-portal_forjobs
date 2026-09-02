package com.aijobportal.job.mapper;

import com.aijobportal.job.dto.JobResponse;
import com.aijobportal.job.entity.Job;

import java.util.List;

public final class JobMapper {

    private JobMapper() {
    }

    public static JobResponse toJob(Job job) {
        return toJob(job, null, null, null, null);
    }

    public static JobResponse toJob(
            Job job,
            Integer matchScore,
            Integer applicantCount,
            Integer shortlistedCount,
            String employerName
    ) {
        List<String> preferred = job.getPreferredSkills();
        List<String> critical = job.getCriticalSkills();
        List<String> educationRequirements = job.getEducationRequirements();
        List<String> certificationRequirements = job.getCertificationRequirements();
        return new JobResponse(
                job.getId(),
                job.getRole(),
                job.getCompanyName(),
                job.getExperience(),
                job.getSkills(),
                critical == null || critical.isEmpty() ? null : critical,
                preferred == null || preferred.isEmpty() ? null : preferred,
                educationRequirements == null || educationRequirements.isEmpty() ? null : educationRequirements,
                certificationRequirements == null || certificationRequirements.isEmpty() ? null : certificationRequirements,
                job.getLocation(),
                job.getSalary(),
                job.getJobType(),
                job.getWorkMode(),
                job.getDescription(),
                job.getStatus().name(),
                job.getPostedDate() == null ? null : job.getPostedDate().toString(),
                job.getEmployer() == null ? null : job.getEmployer().getId(),
                matchScore,
                applicantCount,
                shortlistedCount,
                employerName
        );
    }

    public static JobResponse withMatch(JobResponse job, int matchScore) {
        return new JobResponse(
                job.id(), job.role(), job.companyName(), job.experience(), job.skills(), job.criticalSkills(), job.preferredSkills(),
                job.educationRequirements(), job.certificationRequirements(),
                job.location(), job.salary(), job.jobType(), job.workMode(), job.description(), job.status(),
                job.postedDate(), job.employerId(), matchScore, job.applicantCount(), job.shortlistedCount(), job.employerName()
        );
    }
}
