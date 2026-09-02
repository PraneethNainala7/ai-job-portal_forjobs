package com.aijobportal.candidate.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResumeAnalysisResponse(
        String status,
        String fileName,
        String uploadedAt,
        List<String> skills,
        List<String> additionalSkills,
        String experience,
        List<String> titles,
        List<String> education,
        List<String> certifications,
        String seniority,
        List<String> technologies,
        List<String> projects,
        List<String> industries,
        List<String> programmingLanguages,
        List<String> frameworks,
        List<String> databases,
        List<String> cloudTechnologies,
        List<String> tools,
        String error
) {
}
