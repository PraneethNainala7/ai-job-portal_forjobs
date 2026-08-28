package com.aijobportal.job.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record JobInputRequest(
        @NotBlank(message = "Role is required.")
        @Size(min = 2, message = "Role is required.")
        String role,
        @NotBlank(message = "Experience is required.")
        String experience,
        @NotEmpty(message = "Add at least one skill.")
        List<@NotBlank(message = "Add at least one skill.") String> skills,
        List<String> preferredSkills,
        @NotBlank(message = "Location is required.")
        String location,
        @NotBlank(message = "Salary is required.")
        String salary,
        @NotBlank(message = "Job type is required.")
        String jobType,
        String workMode,
        @NotBlank(message = "Description should be at least 20 characters.")
        @Size(min = 20, message = "Description should be at least 20 characters.")
        String description
) {
}
