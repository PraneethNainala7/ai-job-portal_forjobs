package com.aijobportal.ai.matching.skill;

import com.aijobportal.ai.matching.normalization.SkillNormalizationService;
import com.aijobportal.ai.matching.normalization.SkillRelationshipRegistry;
import com.aijobportal.ai.matching.policy.MatchScorePolicy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class SkillMatchingService {

    private final SkillNormalizationService normalizationService;
    private final SkillRelationshipRegistry relationshipRegistry;
    private final MatchScorePolicy policy;

    public SkillMatchingService(
            SkillNormalizationService normalizationService,
            SkillRelationshipRegistry relationshipRegistry,
            MatchScorePolicy policy
    ) {
        this.normalizationService = normalizationService;
        this.relationshipRegistry = relationshipRegistry;
        this.policy = policy;
    }

    public List<SkillMatchDetail> matchTier(List<String> jobSkills, List<String> candidateSkills) {
        if (jobSkills == null || jobSkills.isEmpty()) {
            return List.of();
        }
        List<SkillMatchDetail> details = new ArrayList<>();
        for (String jobSkill : jobSkills) {
            details.add(classify(jobSkill, candidateSkills));
        }
        return details;
    }

    public SkillMatchDetail classify(String jobSkill, List<String> candidateSkills) {
        if (candidateSkills == null || candidateSkills.isEmpty()) {
            return new SkillMatchDetail(jobSkill, null, MatchType.NO_MATCH, 0);
        }

        String bestCandidate = null;
        MatchType bestType = MatchType.NO_MATCH;
        double bestCredit = 0;

        for (String candidateSkill : candidateSkills) {
            if (relationshipRegistry.isNotRelated(jobSkill, candidateSkill)) {
                continue;
            }
            if (normalizationService.exactMatch(candidateSkill, jobSkill)) {
                return new SkillMatchDetail(
                        jobSkill,
                        candidateSkill,
                        MatchType.EXACT_MATCH,
                        policy.getSkillCredits().getExact()
                );
            }
        }

        for (String candidateSkill : candidateSkills) {
            if (relationshipRegistry.isNotRelated(jobSkill, candidateSkill)) {
                continue;
            }
            if (relationshipRegistry.isRelated(jobSkill, candidateSkill)) {
                double credit = policy.getSkillCredits().getRelated();
                if (credit > bestCredit) {
                    bestCredit = credit;
                    bestType = MatchType.RELATED_MATCH;
                    bestCandidate = candidateSkill;
                }
            }
        }

        if (bestType != MatchType.NO_MATCH) {
            return new SkillMatchDetail(jobSkill, bestCandidate, bestType, bestCredit);
        }

        String jobDomain = inferDomain(normalizationService.normalizeKey(jobSkill));
        for (String candidateSkill : candidateSkills) {
            if (relationshipRegistry.isNotRelated(jobSkill, candidateSkill)) {
                continue;
            }
            String registryDomain = relationshipRegistry.transferableDomain(jobSkill, candidateSkill);
            if (registryDomain != null && domainsCompatible(jobDomain, registryDomain, candidateSkill)) {
                double credit = policy.getSkillCredits().getTransferable();
                if (credit > bestCredit) {
                    bestCredit = credit;
                    bestType = MatchType.TRANSFERABLE_MATCH;
                    bestCandidate = candidateSkill;
                }
            }
        }

        if (bestType != MatchType.NO_MATCH) {
            return new SkillMatchDetail(jobSkill, bestCandidate, bestType, bestCredit);
        }

        return new SkillMatchDetail(jobSkill, null, MatchType.NO_MATCH, 0);
    }

    public static double tierCreditSum(List<SkillMatchDetail> details) {
        return details.stream().mapToDouble(SkillMatchDetail::creditMultiplier).sum();
    }

    public static int matchedCount(List<SkillMatchDetail> details) {
        return (int) details.stream().filter(SkillMatchDetail::hasCredit).count();
    }

    public static List<String> missingSkills(List<SkillMatchDetail> details) {
        return details.stream()
                .filter(detail -> !detail.hasCredit())
                .map(SkillMatchDetail::jobSkill)
                .toList();
    }

    public static List<String> matchedSkillLabels(List<SkillMatchDetail> details) {
        List<String> labels = new ArrayList<>();
        for (SkillMatchDetail detail : details) {
            if (detail.hasCredit()) {
                labels.add(detail.jobSkill());
            }
        }
        return labels;
    }

    public static List<String> partiallyMatchedLabels(List<SkillMatchDetail> details) {
        List<String> labels = new ArrayList<>();
        for (SkillMatchDetail detail : details) {
            if (detail.matchType() == MatchType.RELATED_MATCH || detail.matchType() == MatchType.TRANSFERABLE_MATCH) {
                labels.add(detail.jobSkill() + " (~" + detail.candidateSkill() + ")");
            }
        }
        return labels;
    }

    private boolean domainsCompatible(String jobDomain, String registryDomain, String candidateSkill) {
        if (registryDomain == null || registryDomain.isBlank()) {
            return true;
        }
        String candidateDomain = inferDomain(normalizationService.normalizeKey(candidateSkill));
        if ("unknown".equals(candidateDomain)) {
            return true;
        }
        return registryDomain.equals(candidateDomain) || "devops".equals(registryDomain) || "cloud".equals(registryDomain);
    }

    private static String inferDomain(String key) {
        String lower = key.toLowerCase(Locale.ROOT);
        boolean frontend = containsAny(lower, "react", "angular", "vue", "html", "css", "javascript", "typescript", "next.js");
        boolean backend = containsAny(lower, "java", "spring", "node", "python", "c#", "kotlin", "api", "hibernate", "jpa");
        if (frontend && backend) {
            return "fullstack";
        }
        if (frontend) {
            return "frontend";
        }
        if (backend) {
            return "backend";
        }
        if (containsAny(lower, "docker", "kubernetes", "aws", "azure", "gcp")) {
            return "devops";
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
