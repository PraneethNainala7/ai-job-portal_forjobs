package com.aijobportal.ai.matching.model;

import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.candidate.dto.ResumeAnalysisResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CandidateScoringDataMergerTest {

    private static final CandidateProfileResponse BASE_PROFILE = new CandidateProfileResponse(
            "candidate-1",
            "Jane Doe",
            "jane@example.com",
            null,
            null,
            null,
            null,
            List.of(),
            List.of(),
            List.of(),
            null,
            null
    );

    private static final ResumeAnalysisResponse ANALYSIS = new ResumeAnalysisResponse(
            "COMPLETE",
            "resume.pdf",
            null,
            List.of("Java"),
            List.of(),
            "5 years",
            List.of("Software Engineer"),
            List.of("B.Tech Computer Science"),
            List.of("AWS Certified"),
            null,
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            null
    );

    @Test
    void mergeEducationUsesResumeWhenProfileEmpty() {
        List<String> merged = CandidateScoringDataMerger.mergeEducation(BASE_PROFILE, ANALYSIS);
        assertEquals(List.of("B.Tech Computer Science"), merged);
    }

    @Test
    void mergeEducationKeepsProfileFirstWhenBothPresent() {
        CandidateProfileResponse profile = new CandidateProfileResponse(
                BASE_PROFILE.id(), BASE_PROFILE.fullName(), BASE_PROFILE.email(),
                null, null, null, null, List.of(),
                List.of("M.Tech AI"),
                List.of(),
                null, null
        );
        List<String> merged = CandidateScoringDataMerger.mergeEducation(profile, ANALYSIS);
        assertEquals(2, merged.size());
        assertEquals("M.Tech AI", merged.getFirst());
        assertTrue(merged.contains("B.Tech Computer Science"));
    }

    @Test
    void mergeExperienceFallsBackToResumeText() {
        assertEquals("5 years", CandidateScoringDataMerger.mergeExperience(BASE_PROFILE, ANALYSIS));
    }

    @Test
    void mergeTitleFallsBackToResumeTitle() {
        assertEquals("Software Engineer", CandidateScoringDataMerger.mergeTitle(BASE_PROFILE, ANALYSIS));
    }
}
