package com.aijobportal.ai.client;

import com.aijobportal.ai.dto.MatchResultResponse;
import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.candidate.dto.ResumeAnalysisResponse;
import com.aijobportal.common.domain.JobStatus;
import com.aijobportal.job.entity.Job;
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

    @Test
    void mapsRankingsAndDropsOmittedJobs() {
        CandidateProfileResponse profile = new CandidateProfileResponse(
                "cand-1", "Ada", "ada@example.com", null, "Bengaluru", "Backend Engineer",
                "4 years", List.of("Java"), List.of(), List.of(), null, null
        );
        Job ranked = job("job-1", "Backend Engineer", List.of("Java"));
        Job missing = job("job-2", "Frontend Engineer", List.of("React"));
        String raw = """
                {
                  "matches": [
                    {
                      "jobId": "job-1",
                      "matchScore": 120,
                      "strongAreas": ["Java"],
                      "gaps": [],
                      "explanation": "Strong backend overlap."
                    }
                  ]
                }
                """;
        List<MatchResultResponse> matches = ClaudeAiClient.parseRankings(
                mapper, raw, profile, List.of(ranked, missing)
        );
        assertEquals(1, matches.size());
        assertEquals("job-1", matches.getFirst().jobId());
        assertEquals(98, matches.getFirst().matchScore());
    }

    private static Job job(String id, String role, List<String> skills) {
        Job job = new Job();
        job.setId(id);
        job.setRole(role);
        job.setSkills(skills);
        job.setLocation("Bengaluru, India");
        job.setExperience("3+ years");
        job.setStatus(JobStatus.ACTIVE);
        job.setDescription("Build product features.");
        return job;
    }
}
