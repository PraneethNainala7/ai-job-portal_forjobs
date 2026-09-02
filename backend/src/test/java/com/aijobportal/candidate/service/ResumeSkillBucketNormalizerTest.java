package com.aijobportal.candidate.service;

import com.aijobportal.ai.matching.normalization.SkillNormalizationService;
import com.aijobportal.candidate.dto.ResumeAnalysisResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ResumeSkillBucketNormalizerTest {

    private final SkillNormalizationService normalizationService = new SkillNormalizationService();

    @Test
    void keepsFrameworkSkillOutOfTechnologies() {
        ResumeAnalysisResponse raw = sample(
                List.of(),
                List.of(),
                List.of("React", "REST APIs"),
                List.of("React", "Next.js"),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        ResumeAnalysisResponse normalized = ResumeSkillBucketNormalizer.normalize(raw, normalizationService);

        assertEquals(List.of("React", "Next.js"), normalized.frameworks());
        assertEquals(List.of("REST APIs"), normalized.technologies());
        assertFalse(normalized.technologies().contains("React"));
    }

    @Test
    void keepsProgrammingLanguageOutOfPrimarySkills() {
        ResumeAnalysisResponse raw = sample(
                List.of("JavaScript (ES6+)", "React.js"),
                List.of(),
                List.of(),
                List.of(),
                List.of("JavaScript (ES6+)"),
                List.of(),
                List.of(),
                List.of()
        );

        ResumeAnalysisResponse normalized = ResumeSkillBucketNormalizer.normalize(raw, normalizationService);

        assertEquals(List.of("JavaScript (ES6+)"), normalized.programmingLanguages());
        assertEquals(List.of("React.js"), normalized.skills());
    }

    @Test
    void mergesAliasDuplicatesAcrossBuckets() {
        ResumeAnalysisResponse raw = sample(
                List.of("React.js"),
                List.of(),
                List.of(),
                List.of("React"),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        ResumeAnalysisResponse normalized = ResumeSkillBucketNormalizer.normalize(raw, normalizationService);

        assertEquals(List.of("React"), normalized.frameworks());
        assertEquals(List.of(), normalized.skills());
    }

    private static ResumeAnalysisResponse sample(
            List<String> skills,
            List<String> additionalSkills,
            List<String> technologies,
            List<String> frameworks,
            List<String> programmingLanguages,
            List<String> databases,
            List<String> cloudTechnologies,
            List<String> tools
    ) {
        return new ResumeAnalysisResponse(
                "COMPLETE",
                "resume.pdf",
                "2026-01-01T00:00:00Z",
                skills,
                additionalSkills,
                "3 years",
                List.of(),
                List.of(),
                List.of(),
                "Mid-level",
                technologies,
                List.of(),
                List.of(),
                programmingLanguages,
                frameworks,
                databases,
                cloudTechnologies,
                tools,
                null
        );
    }
}
