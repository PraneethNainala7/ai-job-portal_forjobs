package com.aijobportal.ai.client;

import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.common.domain.JobStatus;
import com.aijobportal.job.entity.Job;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeuristicAiClientTest {

    @Test
    void overlappingSkillsIncreaseScore() {
        Job job = new Job();
        job.setId("job-1");
        job.setRole("Frontend Engineer");
        job.setSkills(List.of("React", "TypeScript"));
        job.setLocation("Bengaluru, India");
        job.setStatus(JobStatus.ACTIVE);
        CandidateProfileResponse profile = new CandidateProfileResponse(
                "user-candidate", "Rohan", "candidate@demo.com", null, "Bengaluru", "Frontend Engineer",
                "4 years", List.of("React", "TypeScript"), List.of(), List.of(), null, null
        );
        HeuristicAiClient client = new HeuristicAiClient();
        int score = client.scoreJob(job, profile).matchScore();
        assertTrue(score >= 80);
        assertEquals(0, client.rankJobs(profile, List.of(job)).size());
    }
}
