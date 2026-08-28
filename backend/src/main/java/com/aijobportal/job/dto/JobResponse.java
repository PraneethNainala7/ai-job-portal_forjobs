package com.aijobportal.job.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record JobResponse(
        String id,
        String role,
        String companyName,
        String experience,
        List<String> skills,
        List<String> preferredSkills,
        String location,
        String salary,
        String jobType,
        String workMode,
        String description,
        String status,
        String postedDate,
        String employerId,
        Integer matchScore,
        Integer applicantCount,
        Integer shortlistedCount,
        String employerName
) {
}
