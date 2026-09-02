package com.aijobportal.ai.matching.scoring;

import com.aijobportal.ai.matching.experience.ExperienceTextParser;
import com.aijobportal.ai.matching.experience.ExperienceTextParser.ParseMode;
import com.aijobportal.ai.matching.model.NormalizedJobRequirements;

import java.util.List;
import java.util.Locale;

public final class DimensionApplicability {

    private DimensionApplicability() {
    }

    public static boolean requiredSkillsApplicable(
            NormalizedJobRequirements job,
            List<String> extractedDescriptionSkills
    ) {
        return !job.criticalSkills().isEmpty()
                || !job.requiredSkills().isEmpty()
                || !extractedDescriptionSkills.isEmpty();
    }

    public static boolean preferredSkillsApplicable(NormalizedJobRequirements job) {
        return job.preferredSkills() != null && !job.preferredSkills().isEmpty();
    }

    public static boolean experienceApplicable(NormalizedJobRequirements job) {
        if (job.minExperienceYears() > 0) {
            return true;
        }
        String text = job.experienceText();
        if (text == null || text.isBlank()) {
            return false;
        }
        return ExperienceTextParser.parseYears(text, ParseMode.MIN_YEARS) > 0
                || text.toLowerCase(Locale.ROOT).matches(".*\\d+\\+?\\s*(years?|yrs?).*");
    }

    public static boolean roleApplicable(NormalizedJobRequirements job) {
        String role = job.role();
        return role != null && role.trim().length() >= 3;
    }

    public static boolean educationApplicable(NormalizedJobRequirements job) {
        return !job.educationRequirements().isEmpty() || !job.certificationRequirements().isEmpty();
    }
}
