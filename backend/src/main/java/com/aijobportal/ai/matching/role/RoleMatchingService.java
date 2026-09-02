package com.aijobportal.ai.matching.role;

import com.aijobportal.ai.matching.model.NormalizedCandidateProfile;
import com.aijobportal.ai.matching.model.NormalizedJobRequirements;
import com.aijobportal.ai.matching.policy.MatchScorePolicy;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class RoleMatchingService {

    private final MatchScorePolicy policy;

    public RoleMatchingService(MatchScorePolicy policy) {
        this.policy = policy;
    }

    public int score(NormalizedCandidateProfile candidate, NormalizedJobRequirements job) {
        int maxPoints = policy.getWeights().getRoleRelevance();
        String jobRole = job.role() == null ? "" : job.role().toLowerCase(Locale.ROOT);
        String title = candidate.title() == null ? "" : candidate.title().toLowerCase(Locale.ROOT);
        if (!candidate.titles().isEmpty()) {
            title = (title + " " + String.join(" ", candidate.titles())).toLowerCase(Locale.ROOT);
        }

        String jobDomain = domainOf(jobRole);
        String candidateDomain = domainOf(title + " " + String.join(" ", candidate.skills()));

        if (jobDomain.equals("unknown") || candidateDomain.equals("unknown")) {
            return tokenOverlapScore(title, jobRole, maxPoints);
        }
        if (jobDomain.equals(candidateDomain)) {
            return maxPoints;
        }
        if (jobDomain.equals("fullstack") || candidateDomain.equals("fullstack")) {
            return (int) Math.round(maxPoints * 0.6);
        }
        return (int) Math.round(maxPoints * 0.2);
    }

    private static int tokenOverlapScore(String left, String right, int max) {
        String[] tokens = left.split("\\s+");
        int hits = 0;
        for (String token : tokens) {
            if (token.length() > 2 && right.contains(token)) {
                hits++;
            }
        }
        return Math.min(max, hits * 2);
    }

    private static String domainOf(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        boolean frontend = containsAny(lower, "frontend", "react", "angular", "vue", "ui", "javascript", "typescript");
        boolean backend = containsAny(lower, "backend", "java", "spring", "node", "api", "microservice", "c#");
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
