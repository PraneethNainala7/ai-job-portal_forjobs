package com.aijobportal.ai.matching;

import com.aijobportal.ai.matching.education.EducationMatchingService;
import com.aijobportal.ai.matching.experience.ExperienceMatchingService;
import com.aijobportal.ai.matching.model.NormalizedCandidateProfile;
import com.aijobportal.ai.matching.model.NormalizedJobRequirements;
import com.aijobportal.ai.matching.normalization.SkillNormalizationService;
import com.aijobportal.ai.matching.normalization.SkillRelationshipRegistry;
import com.aijobportal.ai.matching.policy.MatchScorePolicy;
import com.aijobportal.ai.matching.role.RoleMatchingService;
import com.aijobportal.ai.matching.scoring.MatchAnalysisResult;
import com.aijobportal.ai.matching.scoring.MatchScoringService;
import com.aijobportal.ai.matching.skill.MatchType;
import com.aijobportal.ai.matching.skill.SkillMatchingService;
import com.aijobportal.common.domain.JobStatus;
import com.aijobportal.job.entity.Job;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillNormalizationServiceTest {

    private final SkillNormalizationService normalizer = new SkillNormalizationService();

    @Test
    void javaDoesNotMatchJavaScript() {
        assertFalse(normalizer.exactMatch("JavaScript", "Java"));
        assertFalse(normalizer.exactMatch("Java", "JavaScript"));
        assertFalse(normalizer.exactMatch("JS", "Java"));
    }

    @Test
    void javaMatchesJava() {
        assertTrue(normalizer.exactMatch("Java", "java"));
    }

    @Test
    void springBootAliasesMatch() {
        assertTrue(normalizer.exactMatch("SpringBoot", "Spring Boot"));
    }
}

class MatchScoringServiceTest {

    private MatchScoringService scoringService;

    @BeforeEach
    void setUp() {
        SkillNormalizationService normalizationService = new SkillNormalizationService();
        SkillRelationshipRegistry registry = new SkillRelationshipRegistry(normalizationService);
        registry.load();

        MatchScorePolicy policy = new MatchScorePolicy();
        SkillMatchingService skillMatchingService = new SkillMatchingService(
                normalizationService,
                registry,
                policy
        );
        scoringService = new MatchScoringService(
                policy,
                skillMatchingService,
                new ExperienceMatchingService(policy),
                new RoleMatchingService(policy),
                new EducationMatchingService(policy)
        );
    }

    @Test
    void javaResumeVsJavaJobScoresHigh() {
        NormalizedCandidateProfile candidate = profile(
                "Java Developer",
                "5 years",
                List.of("Java", "Spring Boot", "SQL", "Hibernate", "REST")
        );
        NormalizedJobRequirements job = requirements(
                "Java Developer",
                List.of("Java", "Spring Boot"),
                List.of("Java", "Spring Boot", "Hibernate", "SQL"),
                List.of("Docker")
        );

        MatchAnalysisResult result = scoringService.score(candidate, job);
        assertTrue(result.totalScore() >= 75, "Expected high score, got " + result.totalScore());
    }

    @Test
    void reactResumeVsJavaJobScoresLowWithCriticalCap() {
        NormalizedCandidateProfile candidate = profile(
                "React Developer",
                "4 years",
                List.of("React", "JavaScript", "TypeScript", "Next.js", "HTML", "CSS")
        );
        NormalizedJobRequirements job = requirements(
                "Java Developer",
                List.of("Java", "Spring Boot"),
                List.of("Spring Security", "JPA", "Hibernate", "SQL"),
                List.of("Docker", "Kubernetes")
        );

        MatchAnalysisResult result = scoringService.score(candidate, job);
        assertTrue(result.totalScore() <= 30, "Expected low score, got " + result.totalScore());
        assertTrue(result.scoreCapApplied());
        assertTrue(result.criticalMatches().stream().noneMatch(detail -> detail.matchType() == MatchType.EXACT_MATCH));
    }

    @Test
    void partialJavaResumeGetsPartialCredit() {
        NormalizedCandidateProfile candidate = profile(
                "Software Developer",
                "3 years",
                List.of("Java", "SQL")
        );
        NormalizedJobRequirements job = requirements(
                "Java Developer",
                List.of(),
                List.of("Java", "Spring Boot", "Hibernate"),
                List.of()
        );

        MatchAnalysisResult result = scoringService.score(candidate, job);
        assertTrue(result.totalScore() >= 20 && result.totalScore() <= 55,
                "Expected partial score, got " + result.totalScore());
        assertTrue(result.requiredMatches().stream().anyMatch(d -> d.jobSkill().equalsIgnoreCase("Java")
                && d.matchType() == MatchType.EXACT_MATCH));
        assertTrue(result.requiredMatches().stream().anyMatch(d -> d.jobSkill().equalsIgnoreCase("Spring Boot")
                && d.matchType() == MatchType.NO_MATCH));
    }

    @Test
    void csharpVsJavaJobGetsTransferableCreditOnly() {
        NormalizedCandidateProfile candidate = profile(
                "Backend Developer",
                "4 years",
                List.of("C#", ".NET", "SQL")
        );
        NormalizedJobRequirements job = requirements(
                "Java Developer",
                List.of("Java"),
                List.of("Java", "Spring Boot"),
                List.of()
        );

        MatchAnalysisResult result = scoringService.score(candidate, job);
        assertTrue(result.criticalMatches().stream().anyMatch(detail ->
                detail.jobSkill().equalsIgnoreCase("Java")
                        && detail.matchType() == MatchType.TRANSFERABLE_MATCH));
        assertFalse(result.criticalMatches().stream().anyMatch(detail ->
                detail.jobSkill().equalsIgnoreCase("Java")
                        && detail.matchType() == MatchType.EXACT_MATCH));
    }

    @Test
    void springFrameworkVsSpringBootJobGetsRelatedCredit() {
        NormalizedCandidateProfile candidate = profile(
                "Java Developer",
                "5 years",
                List.of("Java", "Spring Framework", "SQL")
        );
        NormalizedJobRequirements job = requirements(
                "Java Developer",
                List.of(),
                List.of("Java", "Spring Boot"),
                List.of()
        );

        MatchAnalysisResult result = scoringService.score(candidate, job);
        assertTrue(result.requiredMatches().stream().anyMatch(detail ->
                detail.jobSkill().equalsIgnoreCase("Spring Boot")
                        && detail.matchType() == MatchType.RELATED_MATCH));
    }

    @Test
    void scoringIsDeterministic() {
        NormalizedCandidateProfile candidate = profile(
                "Frontend Engineer",
                "2 years",
                List.of("React", "TypeScript")
        );
        NormalizedJobRequirements job = requirements(
                "Backend Engineer",
                List.of("Java"),
                List.of("Java", "Spring Boot"),
                List.of()
        );

        int first = scoringService.score(candidate, job).totalScore();
        for (int i = 0; i < 10; i++) {
            assertEquals(first, scoringService.score(candidate, job).totalScore());
        }
    }

    private static NormalizedCandidateProfile profile(String title, String experience, List<String> skills) {
        return new NormalizedCandidateProfile(
                "candidate-1",
                title,
                experience,
                skills,
                List.of(),
                List.of(),
                List.of(title),
                Map.of()
        );
    }

    private static NormalizedJobRequirements requirements(
            String role,
            List<String> critical,
            List<String> required,
            List<String> preferred
    ) {
        Job job = new Job();
        job.setId("job-1");
        job.setRole(role);
        job.setCriticalSkills(critical);
        job.setSkills(required);
        job.setPreferredSkills(preferred);
        job.setExperience("3+ years");
        job.setLocation("Remote");
        job.setDescription("Build backend services with Java and Spring Boot.");
        job.setStatus(JobStatus.ACTIVE);
        return NormalizedJobRequirements.from(job);
    }
}
