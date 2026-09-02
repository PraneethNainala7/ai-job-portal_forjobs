package com.aijobportal.ai.matching;

import com.aijobportal.job.entity.Job;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @deprecated Use {@link com.aijobportal.ai.matching.scoring.MatchScoringService} instead.
 */
@Deprecated
public final class MatchScoreCalculator {

    private static final Set<String> TRANSFERABLE = Set.of(
            "rest", "git", "agile", "sql", "api", "microservices", "docker", "kubernetes", "aws"
    );

    private static final Set<String> CORE_BACKEND = Set.of("java", "spring boot");

    private static final Pattern YEARS_PATTERN = Pattern.compile("(\\d+)\\+?\\s*(?:years?|yrs?)");

    private MatchScoreCalculator() {
    }

    public static SkillMatchResult matchSkills(CandidateSkillProfile candidate, Job job) {
        List<String> required = job.getSkills() == null ? List.of() : job.getSkills();
        List<String> preferred = job.getPreferredSkills() == null ? List.of() : job.getPreferredSkills();
        List<String> candidateSkills = candidate.skills();

        List<String> matchedRequired = new ArrayList<>();
        List<String> missingRequired = new ArrayList<>();
        for (String requiredSkill : required) {
            if (hasSkill(candidateSkills, requiredSkill)) {
                matchedRequired.add(requiredSkill);
            } else {
                missingRequired.add(requiredSkill);
            }
        }

        List<String> matchedPreferred = new ArrayList<>();
        List<String> missingPreferred = new ArrayList<>();
        for (String preferredSkill : preferred) {
            if (hasSkill(candidateSkills, preferredSkill)) {
                matchedPreferred.add(preferredSkill);
            } else {
                missingPreferred.add(preferredSkill);
            }
        }

        List<String> partiallyRelevant = new ArrayList<>();
        for (String skill : candidateSkills) {
            String key = SkillNormalizer.normalizeKey(skill);
            if (TRANSFERABLE.contains(key) && !matchedRequired.contains(skill) && !matchedPreferred.contains(skill)) {
                if (mentionsInJobText(key, job)) {
                    partiallyRelevant.add(skill);
                }
            }
        }

        return new SkillMatchResult(
                matchedRequired,
                missingRequired,
                matchedPreferred,
                missingPreferred,
                partiallyRelevant
        );
    }

    public static MatchScoreBreakdown calculate(CandidateSkillProfile candidate, Job job) {
        SkillMatchResult skills = matchSkills(candidate, job);
        int requiredCount = job.getSkills() == null ? 0 : job.getSkills().size();
        int preferredCount = job.getPreferredSkills() == null ? 0 : job.getPreferredSkills().size();

        int requiredSkills = requiredCount == 0
                ? scoreDescriptionSkillFallback(candidate, job, 40)
                : (int) Math.round((skills.matchedRequired().size() / (double) requiredCount) * 40);

        int preferredSkills = preferredCount == 0
                ? 0
                : (int) Math.round((skills.matchedPreferred().size() / (double) preferredCount) * 10);

        int experience = scoreExperience(candidate.experience(), job.getExperience());
        int roleRelevance = scoreRoleRelevance(candidate, job);
        int semanticSimilarity = scoreSemanticSimilarity(candidate.skills(), job.getDescription());
        int education = scoreEducation(candidate, job.getDescription());

        MatchScoreBreakdown breakdown = new MatchScoreBreakdown(
                requiredSkills,
                preferredSkills,
                experience,
                roleRelevance,
                semanticSimilarity,
                education
        );

        int total = applyGuardrails(breakdown.total(), skills, job);
        return scaleBreakdownToTotal(breakdown, total);
    }

    private static boolean hasSkill(List<String> candidateSkills, String jobSkill) {
        for (String candidateSkill : candidateSkills) {
            if (SkillNormalizer.matches(candidateSkill, jobSkill)) {
                return true;
            }
        }
        return false;
    }

    private static int scoreDescriptionSkillFallback(CandidateSkillProfile candidate, Job job, int maxPoints) {
        if (job.getDescription() == null || job.getDescription().isBlank()) {
            return 0;
        }
        int hits = 0;
        for (String skill : candidate.skills()) {
            if (mentionsInJobText(SkillNormalizer.normalizeKey(skill), job)) {
                hits++;
            }
        }
        if (hits == 0) {
            return 0;
        }
        return Math.min(maxPoints, hits * 8);
    }

    private static int scoreExperience(String candidateExperience, String jobExperience) {
        int candidateYears = parseYears(candidateExperience);
        int requiredYears = parseYears(jobExperience);
        if (candidateYears <= 0 || requiredYears <= 0) {
            return candidateExperience != null && !candidateExperience.isBlank() ? 10 : 0;
        }
        if (candidateYears >= requiredYears) {
            return 25;
        }
        if (candidateYears >= requiredYears - 1) {
            return 18;
        }
        if (candidateYears >= Math.max(1, requiredYears / 2)) {
            return 10;
        }
        return 4;
    }

    private static int scoreRoleRelevance(CandidateSkillProfile candidate, Job job) {
        String jobRole = job.getRole() == null ? "" : job.getRole().toLowerCase(Locale.ROOT);
        String title = candidate.title() == null ? "" : candidate.title().toLowerCase(Locale.ROOT);
        if (!candidate.titles().isEmpty()) {
            title = (title + " " + String.join(" ", candidate.titles())).toLowerCase(Locale.ROOT);
        }

        String jobDomain = domainOf(jobRole);
        String candidateDomain = domainOf(title + " " + String.join(" ", candidate.skills()));

        if (jobDomain.equals("unknown") || candidateDomain.equals("unknown")) {
            return tokenOverlapScore(title, jobRole, 15);
        }
        if (jobDomain.equals(candidateDomain)) {
            return 15;
        }
        if (jobDomain.equals("fullstack") || candidateDomain.equals("fullstack")) {
            return 8;
        }
        return 2;
    }

    private static int scoreSemanticSimilarity(List<String> skills, String description) {
        if (description == null || description.isBlank() || skills.isEmpty()) {
            return 0;
        }
        String text = description.toLowerCase(Locale.ROOT);
        int hits = 0;
        for (String skill : skills) {
            String key = SkillNormalizer.normalizeKey(skill);
            if (!key.isBlank() && text.contains(key)) {
                hits++;
            }
        }
        return Math.min(5, hits);
    }

    private static int scoreEducation(CandidateSkillProfile candidate, String description) {
        if (description == null || description.isBlank()) {
            return 0;
        }
        String text = description.toLowerCase(Locale.ROOT);
        int hits = 0;
        for (String item : candidate.education()) {
            if (item != null && text.contains(item.toLowerCase(Locale.ROOT))) {
                hits++;
            }
        }
        for (String item : candidate.certifications()) {
            if (item != null && text.contains(item.toLowerCase(Locale.ROOT))) {
                hits++;
            }
        }
        return Math.min(5, hits * 2);
    }

    private static int applyGuardrails(int total, SkillMatchResult skills, Job job) {
        int requiredCount = job.getSkills() == null ? 0 : job.getSkills().size();
        double ratio = skills.requiredMatchRatio(requiredCount);
        int capped = total;

        if (requiredCount > 0 && ratio < 0.30) {
            capped = Math.min(capped, 35);
        }
        if (hasCoreBackendRequirement(job) && !hasCoreBackendMatch(skills)) {
            capped = Math.min(capped, 25);
        }
        return Math.max(0, Math.min(100, capped));
    }

    private static boolean hasCoreBackendRequirement(Job job) {
        if (job.getSkills() == null) {
            return false;
        }
        for (String skill : job.getSkills()) {
            String key = SkillNormalizer.normalizeKey(skill);
            if (CORE_BACKEND.contains(key)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasCoreBackendMatch(SkillMatchResult skills) {
        for (String skill : skills.matchedRequired()) {
            String key = SkillNormalizer.normalizeKey(skill);
            if (CORE_BACKEND.contains(key)) {
                return true;
            }
        }
        return false;
    }

    private static MatchScoreBreakdown scaleBreakdownToTotal(MatchScoreBreakdown breakdown, int targetTotal) {
        int raw = breakdown.total();
        if (raw == targetTotal || raw == 0) {
            if (raw == 0) {
                return new MatchScoreBreakdown(0, 0, 0, 0, 0, 0);
            }
            return breakdown;
        }
        double factor = targetTotal / (double) raw;
        return new MatchScoreBreakdown(
                scale(breakdown.requiredSkills(), factor),
                scale(breakdown.preferredSkills(), factor),
                scale(breakdown.experience(), factor),
                scale(breakdown.roleRelevance(), factor),
                scale(breakdown.semanticSimilarity(), factor),
                scale(breakdown.education(), factor)
        );
    }

    private static int scale(int value, double factor) {
        return (int) Math.round(value * factor);
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
        boolean backend = containsAny(lower, "backend", "java", "spring", "node", "api", "microservice");
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

    private static int tokenOverlapScore(String left, String right, int max) {
        String[] tokens = left.split("\\s+");
        int hits = 0;
        for (String token : tokens) {
            if (token.length() > 2 && right.contains(token)) {
                hits++;
            }
        }
        return Math.min(max, hits * 4);
    }

    private static boolean mentionsInJobText(String key, Job job) {
        String text = ((job.getDescription() == null ? "" : job.getDescription()) + " "
                + (job.getRole() == null ? "" : job.getRole())).toLowerCase(Locale.ROOT);
        return !key.isBlank() && text.contains(key);
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
