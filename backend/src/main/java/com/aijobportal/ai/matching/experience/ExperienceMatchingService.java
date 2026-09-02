package com.aijobportal.ai.matching.experience;

import com.aijobportal.ai.matching.model.NormalizedCandidateProfile;
import com.aijobportal.ai.matching.model.NormalizedJobRequirements;
import com.aijobportal.ai.matching.policy.MatchScorePolicy;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ExperienceMatchingService {

    private static final Pattern YEARS_PATTERN = Pattern.compile("(\\d+)\\+?\\s*(?:years?|yrs?)");

    private final MatchScorePolicy policy;

    public ExperienceMatchingService(MatchScorePolicy policy) {
        this.policy = policy;
    }

    public int score(NormalizedCandidateProfile candidate, NormalizedJobRequirements job) {
        int maxPoints = policy.getWeights().getExperience();
        int requiredYears = job.minExperienceYears();
        int candidateYears = resolveCandidateYears(candidate, job);

        if (requiredYears <= 0) {
            return candidateYears > 0 ? maxPoints / 2 : 0;
        }
        if (candidateYears <= 0) {
            return candidate.experience() != null && !candidate.experience().isBlank() ? maxPoints / 4 : 0;
        }

        String jobDomain = domainOf(job.role() + " " + String.join(" ", job.requiredSkills()) + " " + String.join(" ", job.criticalSkills()));
        String candidateDomain = domainOf(
                (candidate.title() == null ? "" : candidate.title())
                        + " "
                        + String.join(" ", candidate.titles())
                        + " "
                        + String.join(" ", candidate.skills())
        );

        double domainFactor = domainFactor(jobDomain, candidateDomain);
        double yearFactor;
        if (candidateYears >= requiredYears) {
            yearFactor = 1.0;
        } else if (candidateYears >= requiredYears - 1) {
            yearFactor = 0.75;
        } else if (candidateYears >= Math.max(1, requiredYears / 2)) {
            yearFactor = 0.5;
        } else {
            yearFactor = 0.2;
        }

        return (int) Math.round(maxPoints * yearFactor * domainFactor);
    }

    private int resolveCandidateYears(NormalizedCandidateProfile candidate, NormalizedJobRequirements job) {
        String jobDomain = domainOf(job.role());
        Map<String, Integer> byDomain = candidate.experienceYearsByDomain();
        if (!byDomain.isEmpty()) {
            if ("backend".equals(jobDomain) && byDomain.containsKey("backend")) {
                return byDomain.get("backend");
            }
            if ("frontend".equals(jobDomain) && byDomain.containsKey("frontend")) {
                return byDomain.get("frontend");
            }
            if (byDomain.containsKey("total")) {
                return byDomain.get("total");
            }
        }
        return parseYears(candidate.experience());
    }

    private static double domainFactor(String jobDomain, String candidateDomain) {
        if ("unknown".equals(jobDomain) || "unknown".equals(candidateDomain)) {
            return 0.6;
        }
        if (jobDomain.equals(candidateDomain)) {
            return 1.0;
        }
        if ("fullstack".equals(jobDomain) || "fullstack".equals(candidateDomain)) {
            return 0.6;
        }
        return 0.2;
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

    private static String domainOf(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        boolean frontend = containsAny(lower, "frontend", "react", "angular", "vue", "ui", "javascript", "typescript");
        boolean backend = containsAny(lower, "backend", "java", "spring", "node", "api", "microservice", "c#", "python");
        if (frontend && backend) {
            return "fullstack";
        }
        if (frontend) {
            return "frontend";
        }
        if (backend) {
            return "backend";
        }
        return "unknown";
    }

    private static boolean containsAny(String text, String... tokens) {
        for (String token : tokens) {
            if (text.contains(token)) {
                return true;
            }
        }
        return false;
    }
}
