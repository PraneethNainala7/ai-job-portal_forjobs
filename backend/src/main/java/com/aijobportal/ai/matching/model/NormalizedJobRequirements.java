package com.aijobportal.ai.matching.model;

import com.aijobportal.ai.matching.experience.ExperienceTextParser;
import com.aijobportal.ai.matching.experience.ExperienceTextParser.ParseMode;
import com.aijobportal.job.entity.Job;

import java.util.List;

public record NormalizedJobRequirements(
        List<String> criticalSkills,
        List<String> requiredSkills,
        List<String> preferredSkills,
        List<String> educationRequirements,
        List<String> certificationRequirements,
        String role,
        String experienceText,
        String description,
        int minExperienceYears
) {
    public static NormalizedJobRequirements from(Job job) {
        List<String> critical = job.getCriticalSkills() == null ? List.of() : job.getCriticalSkills();
        List<String> required = job.getSkills() == null ? List.of() : job.getSkills();
        List<String> preferred = job.getPreferredSkills() == null ? List.of() : job.getPreferredSkills();
        List<String> educationRequirements = job.getEducationRequirements() == null ? List.of() : job.getEducationRequirements();
        List<String> certificationRequirements = job.getCertificationRequirements() == null ? List.of() : job.getCertificationRequirements();
        String experience = job.getExperience() == null ? "" : job.getExperience();
        return new NormalizedJobRequirements(
                List.copyOf(critical),
                List.copyOf(required),
                List.copyOf(preferred),
                List.copyOf(educationRequirements),
                List.copyOf(certificationRequirements),
                job.getRole(),
                experience,
                job.getDescription(),
                parseYears(experience)
        );
    }

    public int combinedRequiredCount() {
        return criticalSkills.size() + requiredSkills.size();
    }

    private static int parseYears(String value) {
        return ExperienceTextParser.parseYears(value, ParseMode.MIN_YEARS);
    }
}
