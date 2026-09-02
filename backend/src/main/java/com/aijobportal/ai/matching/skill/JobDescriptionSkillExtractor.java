package com.aijobportal.ai.matching.skill;

import com.aijobportal.ai.matching.normalization.SkillNormalizationService;
import com.aijobportal.ai.matching.normalization.SkillRelationshipRegistry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class JobDescriptionSkillExtractor {

    private final SkillNormalizationService normalizationService;
    private final SkillRelationshipRegistry relationshipRegistry;

    public JobDescriptionSkillExtractor(
            SkillNormalizationService normalizationService,
            SkillRelationshipRegistry relationshipRegistry
    ) {
        this.normalizationService = normalizationService;
        this.relationshipRegistry = relationshipRegistry;
    }

    public List<String> extract(String role, String description) {
        String corpus = ((role == null ? "" : role) + " " + (description == null ? "" : description)).toLowerCase(Locale.ROOT);
        if (corpus.isBlank()) {
            return List.of();
        }
        Set<String> found = new LinkedHashSet<>();
        for (String skill : knownSkillVocabulary()) {
            if (containsSkillToken(corpus, skill)) {
                found.add(skill);
            }
        }
        return List.copyOf(found);
    }

    private boolean containsSkillToken(String corpus, String skill) {
        String normalized = skill.toLowerCase(Locale.ROOT);
        if (normalized.contains(" ")) {
            return corpus.contains(normalized);
        }
        return Pattern.compile("\\b" + Pattern.quote(normalized) + "\\b", Pattern.CASE_INSENSITIVE)
                .matcher(corpus)
                .find();
    }

    private List<String> knownSkillVocabulary() {
        Set<String> vocabulary = new LinkedHashSet<>(relationshipRegistry.knownSkillLabels());
        List.of(
                "java", "javascript", "typescript", "python", "c#", "kotlin", "go", "rust", "ruby", "php",
                "spring boot", "spring", "spring framework", "hibernate", "jpa", "react", "angular", "vue", "next.js",
                "node.js", "express", "django", "flask", "fastapi", ".net", "sql", "mysql", "postgresql", "mongodb",
                "redis", "docker", "kubernetes", "aws", "azure", "gcp", "git", "rest", "rest api", "microservices",
                "html", "css", "tailwind", "graphql", "kafka", "terraform", "jenkins", "ci/cd"
        ).forEach(vocabulary::add);
        List<String> labels = new ArrayList<>();
        for (String item : vocabulary) {
            if (item != null && !item.isBlank()) {
                labels.add(item.trim());
            }
        }
        labels.sort((left, right) -> Integer.compare(right.length(), left.length()));
        return labels;
    }
}
