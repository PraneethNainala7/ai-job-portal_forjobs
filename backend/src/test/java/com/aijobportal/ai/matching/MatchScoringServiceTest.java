package com.aijobportal.ai.matching;

import com.aijobportal.ai.matching.education.EducationMatchingService;
import com.aijobportal.ai.matching.experience.ExperienceMatchingService;
import com.aijobportal.ai.matching.model.NormalizedCandidateProfile;
import com.aijobportal.ai.matching.model.NormalizedJobRequirements;
import com.aijobportal.ai.matching.normalization.SkillNormalizationService;
import com.aijobportal.ai.matching.normalization.SkillRelationshipRegistry;
import com.aijobportal.ai.matching.policy.MatchScorePolicy;
import com.aijobportal.ai.matching.role.RoleMatchingService;
import com.aijobportal.ai.matching.scoring.DimensionStatus;
import com.aijobportal.ai.matching.scoring.MatchAnalysisResult;
import com.aijobportal.ai.matching.scoring.MatchScoringService;
import com.aijobportal.ai.matching.skill.JobDescriptionSkillExtractor;
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
        JobDescriptionSkillExtractor descriptionSkillExtractor = new JobDescriptionSkillExtractor(
                normalizationService,
                registry
        );
        scoringService = new MatchScoringService(
                policy,
                skillMatchingService,
                new ExperienceMatchingService(policy),
                new RoleMatchingService(policy),
                new EducationMatchingService(policy),
                descriptionSkillExtractor
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
        assertTrue(result.totalScore() >= 20 && result.totalScore() <= 65,
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

    @Test
    void requiredOnlySkillsDoNotApplyCriticalCapWhenPartialMatchExists() {
        NormalizedCandidateProfile candidate = profile(
                "Java Developer",
                "5 years",
                List.of("Java", "SQL")
        );
        NormalizedJobRequirements job = requirements(
                "Java Developer",
                List.of(),
                List.of("Java", "Spring Boot", "Hibernate"),
                List.of()
        );

        MatchAnalysisResult result = scoringService.score(candidate, job);
        assertTrue(result.totalScore() > 30, "Expected no zero-critical cap, got " + result.totalScore());
        assertFalse(result.scoreCapApplied());
    }

    @Test
    void preferredSkillsNotApplicableAreExcludedFromDenominator() {
        NormalizedCandidateProfile candidate = profile(
                "Java Developer",
                "5 years",
                List.of("Java", "Spring Boot", "SQL")
        );
        NormalizedJobRequirements job = requirements(
                "Java Developer",
                List.of(),
                List.of("Java", "Spring Boot"),
                List.of(),
                "3+ years",
                "Build backend services with Java and Spring Boot."
        );

        MatchAnalysisResult result = scoringService.score(candidate, job);
        assertEquals(DimensionStatus.NOT_APPLICABLE, result.scoredBreakdown().preferredSkills().status());
        assertTrue(result.applicableMaximumPoints() < 100);
        assertTrue(result.totalScore() >= 75);
    }

    @Test
    void preferredSkillsApplicableWithNoMatchScoresZeroInDenominator() {
        NormalizedCandidateProfile candidate = profile(
                "Java Developer",
                "5 years",
                List.of("Java", "Spring Boot")
        );
        NormalizedJobRequirements job = requirements(
                "Java Developer",
                List.of(),
                List.of("Java", "Spring Boot"),
                List.of("Docker", "AWS"),
                "3+ years",
                "Build backend services with Java and Spring Boot."
        );

        MatchAnalysisResult result = scoringService.score(candidate, job);
        assertEquals(DimensionStatus.APPLICABLE, result.scoredBreakdown().preferredSkills().status());
        assertEquals(0, result.scoredBreakdown().preferredSkills().earnedOrZero());
        assertEquals(15, result.scoredBreakdown().preferredSkills().maxPoints());
    }

    @Test
    void experienceNotApplicableWhenRequirementMissing() {
        NormalizedCandidateProfile candidate = profile(
                "Java Developer",
                "5 years",
                List.of("Java")
        );
        NormalizedJobRequirements job = requirements(
                "Java Developer",
                List.of(),
                List.of("Java"),
                List.of(),
                "",
                "Java backend role."
        );

        MatchAnalysisResult result = scoringService.score(candidate, job);
        assertEquals(DimensionStatus.NOT_APPLICABLE, result.scoredBreakdown().experience().status());
    }

    @Test
    void noApplicableDimensionsReturnsUnreliableScore() {
        Job job = new Job();
        job.setId("job-empty");
        job.setRole("");
        job.setCriticalSkills(List.of());
        job.setSkills(List.of());
        job.setPreferredSkills(List.of());
        job.setExperience("");
        job.setDescription("General hiring.");
        job.setLocation("Remote");
        job.setStatus(JobStatus.ACTIVE);

        MatchAnalysisResult result = scoringService.score(
                profile("Developer", "3 years", List.of("Java")),
                NormalizedJobRequirements.from(job)
        );

        assertFalse(result.scoreReliable());
        assertEquals(0, result.totalScore());
        assertEquals(0, result.applicableMaximumPoints());
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
            List<String> preferred,
            String experience,
            String description
    ) {
        Job job = new Job();
        job.setId("job-1");
        job.setRole(role);
        job.setCriticalSkills(critical);
        job.setSkills(required);
        job.setPreferredSkills(preferred);
        job.setExperience(experience);
        job.setLocation("Remote");
        job.setDescription(description);
        job.setStatus(JobStatus.ACTIVE);
        return NormalizedJobRequirements.from(job);
    }

    private static NormalizedJobRequirements requirements(
            String role,
            List<String> critical,
            List<String> required,
            List<String> preferred
    ) {
        return requirements(role, critical, required, preferred, "3+ years",
                "Build backend services with Java and Spring Boot.");
    }
}
