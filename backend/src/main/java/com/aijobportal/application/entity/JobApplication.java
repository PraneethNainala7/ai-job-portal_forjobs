package com.aijobportal.application.entity;



import com.aijobportal.auth.entity.User;

import com.aijobportal.common.domain.ApplicationStatus;

import com.aijobportal.common.util.Ids;

import com.aijobportal.job.entity.Job;

import jakarta.persistence.Column;

import jakarta.persistence.Entity;

import jakarta.persistence.EnumType;

import jakarta.persistence.Enumerated;

import jakarta.persistence.FetchType;

import jakarta.persistence.Id;

import jakarta.persistence.JoinColumn;

import jakarta.persistence.ManyToOne;

import jakarta.persistence.PostLoad;

import jakarta.persistence.PrePersist;

import jakarta.persistence.PreUpdate;

import jakarta.persistence.Table;

import jakarta.persistence.Transient;

import org.springframework.data.domain.Persistable;

import org.hibernate.annotations.JdbcTypeCode;

import org.hibernate.type.SqlTypes;



import java.time.Instant;

import java.util.ArrayList;

import java.util.List;

import java.util.Map;



@Entity

@Table(name = "applications")

public class JobApplication implements Persistable<String> {

    @Transient
    private boolean newEntity = true;

    @Id
    @Column(length = 64)
    private String id;



    @ManyToOne(fetch = FetchType.LAZY, optional = false)

    @JoinColumn(name = "job_id", nullable = false)

    private Job job;



    @ManyToOne(fetch = FetchType.LAZY, optional = false)

    @JoinColumn(name = "candidate_id", nullable = false)

    private User candidate;



    @Enumerated(EnumType.STRING)

    @Column(nullable = false, length = 32)

    private ApplicationStatus status;



    @Column(name = "rejection_reason")

    private String rejectionReason;



    @Column(name = "applied_at", nullable = false)

    private Instant appliedAt;



    @Column(name = "updated_at", nullable = false)

    private Instant updatedAt;



    @Column(name = "resume_file_url")

    private String resumeFileUrl;



    @Column(name = "resume_file_name", length = 260)

    private String resumeFileName;



    @Column(name = "resume_file_type", length = 32)

    private String resumeFileType;



    @JdbcTypeCode(SqlTypes.JSON)

    @Column(name = "resume_parsed_data")

    private Map<String, Object> resumeParsedData;



    @Column(name = "match_score")

    private Integer matchScore;



    @JdbcTypeCode(SqlTypes.JSON)

    @Column(name = "match_strong_areas")

    private List<String> matchStrongAreas = new ArrayList<>();



    @JdbcTypeCode(SqlTypes.JSON)

    @Column(name = "match_gaps")

    private List<String> matchGaps = new ArrayList<>();



    @Column(name = "match_explanation")

    private String matchExplanation;



    @PrePersist

    void onCreate() {

        if (id == null) {

            id = Ids.next();

        }

        Instant now = Instant.now();

        if (appliedAt == null) {

            appliedAt = now;

        }

        updatedAt = now;

        if (status == null) {

            status = ApplicationStatus.APPLIED;

        }

        newEntity = false;

    }

    @PostLoad
    void markLoaded() {
        newEntity = false;
    }

    @Override
    public boolean isNew() {
        return newEntity;
    }

    @PreUpdate

    void onUpdate() {

        updatedAt = Instant.now();

    }



    @Override
    public String getId() {
        return id;
    }



    public void setId(String id) {

        this.id = id;

    }



    public Job getJob() {

        return job;

    }



    public void setJob(Job job) {

        this.job = job;

    }



    public User getCandidate() {

        return candidate;

    }



    public void setCandidate(User candidate) {

        this.candidate = candidate;

    }



    public ApplicationStatus getStatus() {

        return status;

    }



    public void setStatus(ApplicationStatus status) {

        this.status = status;

    }



    public String getRejectionReason() {

        return rejectionReason;

    }



    public void setRejectionReason(String rejectionReason) {

        this.rejectionReason = rejectionReason;

    }



    public Instant getAppliedAt() {

        return appliedAt;

    }



    public void setAppliedAt(Instant appliedAt) {

        this.appliedAt = appliedAt;

    }



    public Instant getUpdatedAt() {

        return updatedAt;

    }



    public void setUpdatedAt(Instant updatedAt) {

        this.updatedAt = updatedAt;

    }



    public String getResumeFileUrl() {

        return resumeFileUrl;

    }



    public void setResumeFileUrl(String resumeFileUrl) {

        this.resumeFileUrl = resumeFileUrl;

    }



    public String getResumeFileName() {

        return resumeFileName;

    }



    public void setResumeFileName(String resumeFileName) {

        this.resumeFileName = resumeFileName;

    }



    public String getResumeFileType() {

        return resumeFileType;

    }



    public void setResumeFileType(String resumeFileType) {

        this.resumeFileType = resumeFileType;

    }



    public Map<String, Object> getResumeParsedData() {

        return resumeParsedData;

    }



    public void setResumeParsedData(Map<String, Object> resumeParsedData) {

        this.resumeParsedData = resumeParsedData;

    }



    public Integer getMatchScore() {

        return matchScore;

    }



    public void setMatchScore(Integer matchScore) {

        this.matchScore = matchScore;

    }



    public List<String> getMatchStrongAreas() {

        return matchStrongAreas;

    }



    public void setMatchStrongAreas(List<String> matchStrongAreas) {

        this.matchStrongAreas = matchStrongAreas;

    }



    public List<String> getMatchGaps() {

        return matchGaps;

    }



    public void setMatchGaps(List<String> matchGaps) {

        this.matchGaps = matchGaps;

    }



    public String getMatchExplanation() {

        return matchExplanation;

    }



    public void setMatchExplanation(String matchExplanation) {

        this.matchExplanation = matchExplanation;

    }

}


