package com.aijobportal.ai.client;

import com.aijobportal.ai.dto.InterviewQuestionsResponse;
import com.aijobportal.ai.dto.MatchResultResponse;
import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.candidate.dto.ResumeAnalysisResponse;
import com.aijobportal.common.domain.ResumeStatus;
import com.aijobportal.job.entity.Job;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HeuristicAiClient implements AiClient {

    @Override
    public ResumeAnalysisResponse analyzeResume(
            CandidateProfileResponse profile,
            ResumeAnalysisResponse current,
            String resumeText
    ) {
        if (current.fileName() != null && current.fileName().toLowerCase(Locale.ROOT).matches(".*\\bfail\\b.*")) {
            return new ResumeAnalysisResponse(
                    ResumeStatus.FAILED.name(),
                    current.fileName(),
                    current.uploadedAt(),
                    List.of(),
                    List.of(),
                    null,
                    List.of(),
                    List.of(),
                    List.of(),
                    null,
                    List.of(),
                    List.of(),
                    List.of(),
                    "The file could not be parsed. Try a text-based PDF, DOC, or DOCX."
            );
        }
        List<String> skills = profile.skills() != null && !profile.skills().isEmpty()
                ? profile.skills()
                : List.of("Communication", "Problem solving");
        return new ResumeAnalysisResponse(
                ResumeStatus.COMPLETE.name(),
                current.fileName(),
                current.uploadedAt(),
                skills,
                List.of("Git", "Agile"),
                profile.experience() != null ? profile.experience() : "Not specified",
                profile.title() != null && !profile.title().isBlank() ? List.of(profile.title()) : List.of(),
                profile.education() == null ? List.of() : profile.education(),
                profile.certifications() == null ? List.of() : profile.certifications(),
                "Mid-level",
                skills.stream().limit(4).toList(),
                profile.title() != null && !profile.title().isBlank()
                        ? List.of("Recent work as " + profile.title())
                        : List.of("Professional project history"),
                List.of("Technology"),
                null
        );
    }

    @Override
    public MatchResultResponse scoreJob(Job job, CandidateProfileResponse profile) {
        List<String> skills = withResumeSkills(profile);
        List<String> strongAreas = job.getSkills().stream()
                .filter(skill -> skills.stream().anyMatch(item -> item.equalsIgnoreCase(skill)))
                .toList();
        List<String> gaps = job.getSkills().stream()
                .filter(skill -> skills.stream().noneMatch(item -> item.equalsIgnoreCase(skill)))
                .toList();
        double overlap = strongAreas.size() / (double) Math.max(job.getSkills().size(), 1);
        int titleBoost = 0;
        if (profile != null && profile.title() != null && !profile.title().isBlank()) {
            String first = profile.title().toLowerCase(Locale.ROOT).split(" ")[0];
            if (job.getRole().toLowerCase(Locale.ROOT).contains(first)) {
                titleBoost = 8;
            }
        }
        int locationBoost = 0;
        if (profile != null && profile.location() != null && !profile.location().isBlank()
                && job.getLocation().toLowerCase(Locale.ROOT).contains(profile.location().toLowerCase(Locale.ROOT))) {
            locationBoost = 6;
        }
        int matchScore = (int) Math.round(Math.min(98, 42 + overlap * 42 + titleBoost + locationBoost));
        String explanation = strongAreas.isEmpty()
                ? "AI insight: limited skill overlap on the current profile. Review the role before you apply or shortlist."
                : "AI insight: overlap on " + String.join(", ", strongAreas) + ". This is decision support, not a hiring decision.";
        return new MatchResultResponse(
                job.getId(),
                profile == null ? "guest" : profile.id(),
                matchScore,
                strongAreas,
                gaps,
                explanation
        );
    }

    @Override
    public List<MatchResultResponse> rankJobs(CandidateProfileResponse profile, List<Job> jobs) {
        return List.of();
    }

    @Override
    public InterviewQuestionsResponse interviewQuestions(Job job, CandidateProfileResponse candidate) {
        String skill = job.getSkills().isEmpty() ? "this stack" : job.getSkills().get(0);
        String candidateSkill = candidate.skills() == null || candidate.skills().isEmpty()
                ? "your strongest skill"
                : candidate.skills().get(0);
        String snippet = job.getDescription().length() > 120 ? job.getDescription().substring(0, 120) : job.getDescription();
        return new InterviewQuestionsResponse(
                List.of(
                        "Walk through how you would design a production feature using " + skill + ".",
                        "What trade-offs would you consider when scaling " + job.getRole().toLowerCase(Locale.ROOT) + " work?"
                ),
                List.of(
                        "Tell us about a recent project that used " + candidateSkill + ".",
                        "How does your " + (candidate.experience() == null ? "experience" : candidate.experience())
                                + " prepare you for " + job.getRole() + "?"
                ),
                List.of(
                        "This role focuses on: " + snippet + ". How would you approach the first 90 days?",
                        "Which required skill for " + job.getRole() + " would you ramp up first, and why?"
                ),
                List.of(
                        "Describe a time you disagreed with a hiring or product decision and how you handled it.",
                        "How do you work with hiring managers when match scores and human judgment differ?"
                )
        );
    }

    public static List<String> withResumeSkills(CandidateProfileResponse profile) {
        if (profile == null || profile.skills() == null) {
            return List.of();
        }
        Set<String> seen = new LinkedHashSet<>();
        List<String> merged = new ArrayList<>();
        for (String skill : profile.skills()) {
            String key = skill.toLowerCase(Locale.ROOT);
            if (seen.add(key)) {
                merged.add(skill);
            }
        }
        return merged;
    }
}
