package com.aijobportal.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ai_usage")
public class AiUsage {

    @Id
    @Column(length = 32)
    private String id;

    @Column(name = "resume_analysis", nullable = false)
    private int resumeAnalysis;

    @Column(name = "job_match", nullable = false)
    private int jobMatch;

    @Column(nullable = false)
    private int recommendations;

    @Column(name = "interview_questions", nullable = false)
    private int interviewQuestions;

    @Column(nullable = false)
    private int failures;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getResumeAnalysis() {
        return resumeAnalysis;
    }

    public void setResumeAnalysis(int resumeAnalysis) {
        this.resumeAnalysis = resumeAnalysis;
    }

    public int getJobMatch() {
        return jobMatch;
    }

    public void setJobMatch(int jobMatch) {
        this.jobMatch = jobMatch;
    }

    public int getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(int recommendations) {
        this.recommendations = recommendations;
    }

    public int getInterviewQuestions() {
        return interviewQuestions;
    }

    public void setInterviewQuestions(int interviewQuestions) {
        this.interviewQuestions = interviewQuestions;
    }

    public int getFailures() {
        return failures;
    }

    public void setFailures(int failures) {
        this.failures = failures;
    }
}
