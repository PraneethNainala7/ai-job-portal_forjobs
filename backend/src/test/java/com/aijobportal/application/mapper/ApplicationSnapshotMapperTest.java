package com.aijobportal.application.mapper;

import com.aijobportal.ai.dto.MatchResultResponse;
import com.aijobportal.application.entity.JobApplication;
import com.aijobportal.auth.entity.User;
import com.aijobportal.candidate.entity.CandidateProfile;
import com.aijobportal.job.entity.Job;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationSnapshotMapperTest {

    @Test
    void matchFromApplicationReturnsStoredScore() {
        JobApplication application = new JobApplication();
        application.setJob(job("job-1"));
        application.setCandidate(candidate("c-1"));
        application.setMatchScore(91);
        application.setMatchStrongAreas(List.of("React"));
        application.setMatchGaps(List.of("AWS"));
        application.setMatchExplanation("Strong frontend fit");

        MatchResultResponse match = ApplicationSnapshotMapper.matchFromApplication(application);

        assertEquals(91, match.matchScore());
        assertEquals(List.of("React"), match.strongAreas());
        assertEquals("Strong frontend fit", match.explanation());
    }

    @Test
    void matchFromApplicationReturnsNullForLegacyRows() {
        JobApplication application = new JobApplication();
        application.setJob(job("job-1"));
        application.setCandidate(candidate("c-1"));

        assertNull(ApplicationSnapshotMapper.matchFromApplication(application));
    }

    @Test
    void candidateFromApplicationUsesSnapshotSkills() {
        JobApplication application = new JobApplication();
        application.setCandidate(candidate("c-1"));
        application.setResumeParsedData(Map.of(
                "skills", List.of("TypeScript"),
                "additionalSkills", List.of("Node.js"),
                "experience", "5 years",
                "titles", List.of("Senior Engineer")
        ));

        CandidateProfile profile = new CandidateProfile();
        profile.setUser(application.getCandidate());
        profile.setTitle("Old title");
        profile.setSkills(List.of("Java"));

        var response = ApplicationSnapshotMapper.candidateFromApplication(application, profile);

        assertTrue(response.skills().contains("TypeScript"));
        assertTrue(response.skills().contains("Node.js"));
        assertEquals("5 years", response.experience());
    }

    @Test
    void resumeInfoRequiresStoredFile() {
        JobApplication application = new JobApplication();
        application.setAppliedAt(Instant.parse("2026-01-15T10:00:00Z"));
        application.setResumeFileUrl("/tmp/resume.pdf");
        application.setResumeFileName("resume.pdf");

        var info = ApplicationSnapshotMapper.resumeInfo(application);

        assertEquals("resume.pdf", info.fileName());
        assertTrue(info.downloadable());
    }

    private static User candidate(String id) {
        User user = new User();
        user.setId(id);
        user.setName("Candidate");
        user.setEmail("candidate@test.com");
        return user;
    }

    private static Job job(String id) {
        Job job = new Job();
        job.setId(id);
        return job;
    }
}
