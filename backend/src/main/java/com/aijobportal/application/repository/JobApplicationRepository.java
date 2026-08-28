package com.aijobportal.application.repository;

import com.aijobportal.application.entity.JobApplication;
import com.aijobportal.common.domain.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, String> {

    boolean existsByJobIdAndCandidateId(String jobId, String candidateId);

    @Query("select a from JobApplication a join fetch a.job join fetch a.candidate where a.candidate.id = :candidateId order by a.appliedAt desc")
    List<JobApplication> findByCandidateIdOrderByAppliedAtDesc(String candidateId);

    @Query("select a from JobApplication a join fetch a.job join fetch a.candidate where a.job.id = :jobId order by a.appliedAt desc")
    List<JobApplication> findByJobIdOrderByAppliedAtDesc(String jobId);

    Optional<JobApplication> findByJobIdAndCandidateId(String jobId, String candidateId);

    @Query("select a from JobApplication a join fetch a.job left join fetch a.job.employer join fetch a.candidate where a.id = :id")
    Optional<JobApplication> findDetailedById(String id);

    @Query("select a from JobApplication a join fetch a.job left join fetch a.job.employer join fetch a.candidate")
    List<JobApplication> findAllFetched();

    long countByJobId(String jobId);

    long countByJobIdAndStatusIn(String jobId, List<ApplicationStatus> statuses);

    long countByStatus(ApplicationStatus status);
}
