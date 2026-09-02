package com.aijobportal.ai.client;

import com.aijobportal.candidate.dto.ResumeAnalysisResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClaudeAiClientTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void mapsResumeAnalysisJson() {
        ResumeAnalysisResponse current = new ResumeAnalysisResponse(
                "PROCESSING", "resume.pdf", "2026-01-01T00:00:00Z",
                List.of(), List.of(), null, List.of(), List.of(), List.of(),
                null, List.of(), List.of(), List.of(), null
        );
        String raw = """
                ```json
                {
                  "skills": ["Java", "Spring"],
                  "additionalSkills": ["Git"],
                  "experience": "4 years",
                  "titles": ["Backend Engineer"],
                  "education": ["B.Tech"],
                  "certifications": [],
                  "seniority": "Mid-level",
                  "technologies": ["PostgreSQL"],
                  "projects": ["Job portal"],
                  "industries": ["Technology"]
                }
                ```
                """;
        ResumeAnalysisResponse result = ClaudeAiClient.parseAnalysis(mapper, raw, current);
        assertEquals("COMPLETE", result.status());
        assertEquals("resume.pdf", result.fileName());
        assertEquals(List.of("Java", "Spring"), result.skills());
        assertEquals("4 years", result.experience());
        assertEquals(List.of("PostgreSQL"), result.technologies());
    }
}
