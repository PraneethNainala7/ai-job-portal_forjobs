package com.aijobportal.ai.matching.education;

import com.aijobportal.ai.matching.model.NormalizedCandidateProfile;
import com.aijobportal.ai.matching.model.NormalizedJobRequirements;
import com.aijobportal.ai.matching.policy.MatchScorePolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EducationMatchingServiceTest {

    private EducationMatchingService service;

    @BeforeEach
    void setUp() {
        service = new EducationMatchingService(new MatchScorePolicy());
    }

    @Test
    void structuredEducationRequirementsScoreProportionally() {
        NormalizedCandidateProfile candidate = candidate(
                List.of("B.Tech in Computer Science"),
                List.of()
        );
        NormalizedJobRequirements job = job(
                List.of("B.Tech Computer Science", "Master's degree"),
                List.of(),
                "Any description"
        );

        int score = service.score(candidate, job);
        assertTrue(score >= 2, "Expected partial education score, got " + score);
        assertTrue(score <= 5);
    }

    @Test
    void structuredCertificationRequirementsMatch() {
        NormalizedCandidateProfile candidate = candidate(
                List.of(),
                List.of("AWS Certified Solutions Architect")
        );
        NormalizedJobRequirements job = job(
                List.of(),
                List.of("AWS Certified"),
                "Backend role"
        );

        assertTrue(service.score(candidate, job) >= 2);
    }

    @Test
    void notApplicableWhenEmployerDidNotProvideRequirements() {
        NormalizedJobRequirements job = job(
                List.of(),
                List.of(),
                "Requires B.Tech CS or equivalent."
        );
        assertFalse(EducationMatchingService.isApplicable(job));
        assertEquals(0, service.score(candidate(List.of("B.Tech CS"), List.of()), job));
    }

    private static NormalizedCandidateProfile candidate(List<String> education, List<String> certifications) {
        return new NormalizedCandidateProfile(
                "candidate-1",
                "Developer",
                "4 years",
                List.of("Java"),
                education,
                certifications,
                List.of("Developer"),
                Map.of()
        );
    }

    private static NormalizedJobRequirements job(
            List<String> educationRequirements,
            List<String> certificationRequirements,
            String description
    ) {
        return new NormalizedJobRequirements(
                List.of(),
                List.of("Java"),
                List.of(),
                educationRequirements,
                certificationRequirements,
                "Java Developer",
                "3+ years",
                description,
                3
        );
    }
}
