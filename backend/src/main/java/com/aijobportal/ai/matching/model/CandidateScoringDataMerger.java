package com.aijobportal.ai.matching.model;

import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.candidate.dto.ResumeAnalysisResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CandidateScoringDataMerger {

    private CandidateScoringDataMerger() {
    }

    public static String mergeTitle(CandidateProfileResponse profile, ResumeAnalysisResponse analysis) {
        if (profile.title() != null && !profile.title().isBlank()) {
            return profile.title();
        }
        if (analysis != null && analysis.titles() != null) {
            for (String title : analysis.titles()) {
                if (title != null && !title.isBlank()) {
                    return title.trim();
                }
            }
        }
        return profile.title();
    }

    public static String mergeExperience(CandidateProfileResponse profile, ResumeAnalysisResponse analysis) {
        if (profile.experience() != null && !profile.experience().isBlank()) {
            return profile.experience();
        }
        if (analysis != null && analysis.experience() != null && !analysis.experience().isBlank()) {
            return analysis.experience().trim();
        }
        return profile.experience();
    }

    public static List<String> mergeEducation(CandidateProfileResponse profile, ResumeAnalysisResponse analysis) {
        return mergeLists(profile.education(), analysis == null ? null : analysis.education());
    }

    public static List<String> mergeCertifications(CandidateProfileResponse profile, ResumeAnalysisResponse analysis) {
        return mergeLists(profile.certifications(), analysis == null ? null : analysis.certifications());
    }

    static List<String> mergeLists(List<String> profileValues, List<String> resumeValues) {
        List<String> merged = new ArrayList<>();
        appendUnique(merged, profileValues);
        appendUnique(merged, resumeValues);
        return List.copyOf(merged);
    }

    private static void appendUnique(List<String> target, List<String> values) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            String trimmed = value.trim();
            boolean exists = target.stream()
                    .anyMatch(item -> item.equalsIgnoreCase(trimmed));
            if (!exists) {
                target.add(trimmed);
            }
        }
    }
}
