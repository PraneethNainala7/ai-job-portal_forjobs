package com.aijobportal.ai.matching.normalization;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class SkillRelationshipRegistry {

    private final SkillNormalizationService normalizationService;
    private List<RelationshipRule> related = List.of();
    private List<RelationshipRule> transferable = List.of();
    private List<PairRule> notRelated = List.of();

    public SkillRelationshipRegistry(SkillNormalizationService normalizationService) {
        this.normalizationService = normalizationService;
    }

    @PostConstruct
    public void load() {
        ClassPathResource resource = new ClassPathResource("matching/skill-relationships.yml");
        if (!resource.exists()) {
            return;
        }
        try (InputStream input = resource.getInputStream()) {
            Yaml yaml = new Yaml();
            Object loaded = yaml.load(input);
            if (!(loaded instanceof Map<?, ?> root)) {
                return;
            }
            related = parseRules(root.get("related"));
            transferable = parseRules(root.get("transferable"));
            notRelated = parsePairs(root.get("not-related"));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load skill-relationships.yml", ex);
        }
    }

    public boolean isNotRelated(String jobSkill, String candidateSkill) {
        String jobKey = normalizationService.normalizeKey(jobSkill);
        String candidateKey = normalizationService.normalizeKey(candidateSkill);
        for (PairRule rule : notRelated) {
            if (rule.job().equals(jobKey) && rule.candidate().equals(candidateKey)) {
                return true;
            }
        }
        return false;
    }

    public boolean isRelated(String jobSkill, String candidateSkill) {
        String jobKey = normalizationService.normalizeKey(jobSkill);
        String candidateKey = normalizationService.normalizeKey(candidateSkill);
        for (RelationshipRule rule : related) {
            if (rule.job().equals(jobKey) && rule.candidate().equals(candidateKey)) {
                return true;
            }
        }
        return false;
    }

    public String transferableDomain(String jobSkill, String candidateSkill) {
        String jobKey = normalizationService.normalizeKey(jobSkill);
        String candidateKey = normalizationService.normalizeKey(candidateSkill);
        for (RelationshipRule rule : transferable) {
            if (rule.job().equals(jobKey) && rule.candidate().equals(candidateKey)) {
                return rule.domain();
            }
        }
        return null;
    }

    private List<RelationshipRule> parseRules(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<RelationshipRule> rules = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                String job = normalizeField(map.get("job"));
                String candidate = normalizeField(map.get("candidate"));
                String domain = map.get("domain") == null ? null : normalizeField(map.get("domain"));
                if (!job.isBlank() && !candidate.isBlank()) {
                    rules.add(new RelationshipRule(job, candidate, domain));
                }
            }
        }
        return List.copyOf(rules);
    }

    private List<PairRule> parsePairs(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<PairRule> rules = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                String job = normalizeField(map.get("job"));
                String candidate = normalizeField(map.get("candidate"));
                if (!job.isBlank() && !candidate.isBlank()) {
                    rules.add(new PairRule(job, candidate));
                }
            }
        }
        return List.copyOf(rules);
    }

    private String normalizeField(Object value) {
        if (value == null) {
            return "";
        }
        return normalizationService.normalizeKey(String.valueOf(value));
    }

    record RelationshipRule(String job, String candidate, String domain) {
    }

    record PairRule(String job, String candidate) {
    }
}
