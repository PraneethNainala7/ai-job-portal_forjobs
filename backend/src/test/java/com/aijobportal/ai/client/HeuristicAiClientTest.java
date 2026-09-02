package com.aijobportal.ai.client;

import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.common.domain.JobStatus;
import com.aijobportal.job.entity.Job;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class HeuristicAiClientTest {

    @Test
    void interviewQuestionsUsesJobAndCandidateContext() {
        Job job = new Job();
        job.setId("job-1");
        job.setRole("Frontend Engineer");
        job.setSkills(List.of("React", "TypeScript"));
        job.setCriticalSkills(List.of("React", "TypeScript"));
        job.setLocation("Bengaluru, India");
        job.setDescription("Build customer-facing React applications with TypeScript.");
        job.setStatus(JobStatus.ACTIVE);
        CandidateProfileResponse profile = new CandidateProfileResponse(
                "user-candidate", "Rohan", "candidate@demo.com", null, "Bengaluru", "Frontend Engineer",
                "4 years", List.of("React", "TypeScript"), List.of(), List.of(), null, null
        );
        HeuristicAiClient client = new HeuristicAiClient();
        var questions = client.interviewQuestions(job, profile);
        assertFalse(questions.technical().isEmpty());
        assertFalse(questions.behavioral().isEmpty());
    }
}

class DisabledAiClientTest {

    @Test
    void analyzeResumeFailsWhenAiNotConfigured() {
        DisabledAiClient client = new DisabledAiClient(new HeuristicAiClient());
        var outcome = client.analyzeResume(
                new CandidateProfileResponse(
                        "user-1", "Test", "test@example.com", null, null, null,
                        null, List.of("Java"), List.of(), List.of(), null, null
                ),
                new com.aijobportal.candidate.dto.ResumeAnalysisResponse(
                        "PROCESSING", "resume.pdf", "2026-01-01T00:00:00Z",
                        List.of(), List.of(), null, List.of(), List.of(), List.of(),
                        null, List.of(), List.of(), List.of(),
                        List.of(), List.of(), List.of(), List.of(), List.of(), null
                ),
                "Sample resume text"
        );
        assertEquals("FAILED", outcome.response().status());
        assertEquals(DisabledAiClient.MISSING_API_KEY_MESSAGE, outcome.response().error());
    }
}
