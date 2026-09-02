package com.aijobportal.ai.matching;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class SkillNormalizer {

    private static final Set<String> DISTINCT_TOKENS = Set.of("java", "javascript", "typescript");

    private static final Map<String, String> ALIASES = aliasMap();

    private SkillNormalizer() {
    }

    public static String normalizeKey(String skill) {
        if (skill == null || skill.isBlank()) {
            return "";
        }
        String cleaned = skill.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        return ALIASES.getOrDefault(cleaned, cleaned);
    }

    public static boolean matches(String candidateSkill, String jobSkill) {
        String candidateKey = normalizeKey(candidateSkill);
        String jobKey = normalizeKey(jobSkill);
        if (candidateKey.isBlank() || jobKey.isBlank()) {
            return false;
        }
        if (candidateKey.equals(jobKey)) {
            return true;
        }
        if (isDistinctTokenConflict(candidateKey, jobKey)) {
            return false;
        }
        return canonicalGroup(candidateKey).equals(canonicalGroup(jobKey));
    }

    public static boolean isDistinctTokenConflict(String left, String right) {
        if (left.equals("java") && right.contains("javascript")) {
            return true;
        }
        if (right.equals("java") && left.contains("javascript")) {
            return true;
        }
        if (left.equals("javascript") && right.equals("java")) {
            return true;
        }
        return DISTINCT_TOKENS.contains(left) && DISTINCT_TOKENS.contains(right) && !left.equals(right);
    }

    public static List<String> dedupe(List<String> skills) {
        Map<String, String> seen = new LinkedHashMap<>();
        if (skills == null) {
            return List.of();
        }
        for (String skill : skills) {
            if (skill == null || skill.isBlank()) {
                continue;
            }
            String key = normalizeKey(skill);
            if (!key.isBlank()) {
                seen.putIfAbsent(key, skill.trim());
            }
        }
        return new ArrayList<>(seen.values());
    }

    private static String canonicalGroup(String key) {
        return ALIASES.getOrDefault(key, key);
    }

    private static Map<String, String> aliasMap() {
        Map<String, String> aliases = new LinkedHashMap<>();
        putAlias(aliases, "spring boot", "spring boot");
        putAlias(aliases, "springboot", "spring boot");
        putAlias(aliases, "spring-boot", "spring boot");
        putAlias(aliases, "hibernate", "hibernate");
        putAlias(aliases, "jpa", "jpa");
        putAlias(aliases, "hibernate/jpa", "jpa");
        putAlias(aliases, "hibernate jpa", "jpa");
        putAlias(aliases, "spring security", "spring security");
        putAlias(aliases, "spring-security", "spring security");
        putAlias(aliases, "rest api", "rest");
        putAlias(aliases, "rest apis", "rest");
        putAlias(aliases, "rest", "rest");
        putAlias(aliases, "react.js", "react");
        putAlias(aliases, "reactjs", "react");
        putAlias(aliases, "react", "react");
        putAlias(aliases, "next.js", "next.js");
        putAlias(aliases, "nextjs", "next.js");
        putAlias(aliases, "node.js", "node.js");
        putAlias(aliases, "nodejs", "node.js");
        putAlias(aliases, "javascript", "javascript");
        putAlias(aliases, "js", "javascript");
        putAlias(aliases, "typescript", "typescript");
        putAlias(aliases, "ts", "typescript");
        putAlias(aliases, "java", "java");
        putAlias(aliases, "sql", "sql");
        putAlias(aliases, "postgresql", "sql");
        putAlias(aliases, "postgres", "sql");
        putAlias(aliases, "mysql", "sql");
        putAlias(aliases, "html", "html");
        putAlias(aliases, "css", "css");
        putAlias(aliases, "kubernetes", "kubernetes");
        putAlias(aliases, "k8s", "kubernetes");
        putAlias(aliases, "docker", "docker");
        putAlias(aliases, "aws", "aws");
        putAlias(aliases, "git", "git");
        putAlias(aliases, "agile", "agile");
        putAlias(aliases, "maven", "maven");
        putAlias(aliases, "gradle", "gradle");
        return Map.copyOf(aliases);
    }

    private static void putAlias(Map<String, String> aliases, String alias, String canonical) {
        aliases.put(alias, canonical);
    }
}
