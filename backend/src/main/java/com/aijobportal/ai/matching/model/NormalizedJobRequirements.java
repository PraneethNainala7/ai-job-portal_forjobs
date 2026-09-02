package com.aijobportal.ai.matching.model;

import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.candidate.dto.ResumeAnalysisResponse;
import com.aijobportal.candidate.entity.Resume;
import com.aijobportal.candidate.mapper.CandidateMapper;
import com.aijobportal.common.domain.ResumeStatus;
import com.aijobportal.job.entity.Job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record NormalizedJobRequirements(
        List<String> criticalSkills,
        List<String> requiredSkills,
        List<String> preferredSkills,
        String role,
        String experienceText,
        String description,
        int minExperienceYears
) {
    private static final Pattern YEARS_PATTERN = Pattern.compile("(\\d+)\\+?\\s*(?:years?|yrs?)");

    public static NormalizedJobRequirements from(Job job) {
        List<String> critical = job.getCriticalSkills() == null ? List.of() : job.getCriticalSkills();
        List<String> required = job.getSkills() == null ? List.of() : job.getSkills();
        List<String> preferred = job.getPreferredSkills() == null ? List.of() : job.getPreferredSkills();
        String experience = job.getExperience() == null ? "" : job.getExperience();
        return new NormalizedJobRequirements(
                List.copyOf(critical),
                List.copyOf(required),
                List.copyOf(preferred),
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
        if (value == null || value.isBlank()) {
            return 0;
        }
        Matcher matcher = YEARS_PATTERN.matcher(value.toLowerCase(Locale.ROOT));
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }
}
