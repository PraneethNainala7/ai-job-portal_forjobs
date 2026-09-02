package com.aijobportal.ai.client;

import com.aijobportal.ai.dto.InterviewQuestionsResponse;
import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.job.entity.Job;

import java.util.List;
import java.util.Locale;

/**
 * Template-based interview questions only. Does not perform resume analysis.
 */
public class HeuristicAiClient {

    public InterviewQuestionsResponse interviewQuestions(Job job, CandidateProfileResponse candidate) {
        String skill = primarySkill(job);
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
                        "Which key skill for " + job.getRole() + " would you ramp up first, and why?"
                ),
                List.of(
                        "Describe a time you disagreed with a hiring or product decision and how you handled it.",
                        "How do you work with hiring managers when match scores and human judgment differ?"
                )
        );
    }

    private static String primarySkill(Job job) {
        if (job.getCriticalSkills() != null && !job.getCriticalSkills().isEmpty()) {
            return job.getCriticalSkills().getFirst();
        }
        if (job.getSkills() != null && !job.getSkills().isEmpty()) {
            return job.getSkills().getFirst();
        }
        return "this stack";
    }
}
