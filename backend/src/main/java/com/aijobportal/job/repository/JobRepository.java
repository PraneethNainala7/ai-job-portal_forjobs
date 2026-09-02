package com.aijobportal.job.repository;

import com.aijobportal.common.domain.JobStatus;
import com.aijobportal.job.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aijobportal.common.domain.AccountStatus;

import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, String> {

    List<Job> findByEmployerIdOrderByCreatedAtDesc(String employerId);

    @Query("select j from Job j left join fetch j.employer where j.status = :status order by j.postedDate desc")
    List<Job> findByStatusOrderByPostedDateDesc(@Param("status") JobStatus status);

    @Query("""
            select j from Job j
            join fetch j.employer e
            where j.status = :status and e.accountStatus = :employerStatus
            order by j.postedDate desc
            """)
    List<Job> findPublicActiveJobsOrderByPostedDateDesc(
            @Param("status") JobStatus status,
            @Param("employerStatus") AccountStatus employerStatus
    );

    Optional<Job> findByIdAndEmployerId(String id, String employerId);

    @Query("select j from Job j left join fetch j.employer")
    List<Job> findAllWithEmployer();

    @Query("select j from Job j left join fetch j.employer where j.id = :id")
    Optional<Job> findDetailedById(@Param("id") String id);

    long countByStatus(JobStatus status);

    long countByEmployerId(String employerId);

    long countByEmployerIdAndStatus(String employerId, JobStatus status);
}
