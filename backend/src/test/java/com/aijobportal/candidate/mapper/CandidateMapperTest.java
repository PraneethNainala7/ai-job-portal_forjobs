package com.aijobportal.candidate.mapper;

import com.aijobportal.candidate.entity.Resume;
import com.aijobportal.common.domain.ResumeStatus;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CandidateMapperTest {

    @Test
    void processingResumeDoesNotExposeStaleParsedFields() {
        Resume resume = new Resume();
        resume.setStatus(ResumeStatus.PROCESSING);
        resume.setFileName("new-resume.pdf");
        Map<String, Object> stale = new LinkedHashMap<>();
        stale.put("skills", List.of("Communication", "Problem solving"));
        stale.put("additionalSkills", List.of("Git", "Agile"));
        stale.put("seniority", "Mid-level");
        resume.setParsedData(stale);

        var response = CandidateMapper.toResume(resume);

        assertEquals("PROCESSING", response.status());
        assertTrue(response.skills().isEmpty());
        assertTrue(response.additionalSkills().isEmpty());
    }

    @Test
    void legacyHeuristicStubIsSurfacedAsFailed() {
        Resume resume = new Resume();
        resume.setStatus(ResumeStatus.COMPLETE);
        resume.setFileName("old-resume.pdf");
        resume.setParsedData(CandidateMapper.toParsedData(new com.aijobportal.candidate.dto.ResumeAnalysisResponse(
                ResumeStatus.COMPLETE.name(),
                "old-resume.pdf",
                null,
                List.of("Communication", "Problem solving"),
                List.of("Git", "Agile"),
                "Not specified",
                List.of(),
                List.of(),
                List.of(),
                "Mid-level",
                List.of("Communication", "Problem solving"),
                List.of("Professional project history"),
                List.of("Technology"),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null
        )));

        var response = CandidateMapper.toResume(resume);

        assertEquals("FAILED", response.status());
        assertTrue(response.skills().isEmpty());
        assertTrue(response.error() != null && response.error().contains("outdated"));
    }

    @Test
    void completeResumeExposesExtendedSkillBuckets() {
        Resume resume = new Resume();
        resume.setStatus(ResumeStatus.COMPLETE);
        resume.setFileName("frontend.pdf");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("skills", List.of("React.js", "Next.js"));
        data.put("programmingLanguages", List.of("JavaScript (ES6+)"));
        data.put("tools", List.of("Git", "Postman", "VS Code"));
        data.put("experience", "3 years");
        resume.setParsedData(data);

        var response = CandidateMapper.toResume(resume);

        assertEquals(List.of("JavaScript (ES6+)"), response.programmingLanguages());
        assertEquals(List.of("Git", "Postman", "VS Code"), response.tools());
        assertTrue(CandidateMapper.allExtractedSkills(response).contains("JavaScript (ES6+)"));
        assertTrue(CandidateMapper.allExtractedSkills(response).contains("Git"));
    }
}
