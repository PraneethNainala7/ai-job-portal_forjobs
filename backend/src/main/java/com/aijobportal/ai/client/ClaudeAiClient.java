package com.aijobportal.ai.client;

import com.aijobportal.ai.dto.InterviewQuestionsResponse;
import com.aijobportal.ai.dto.MatchResultResponse;
import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.candidate.dto.ResumeAnalysisResponse;
import com.aijobportal.common.domain.ResumeStatus;
import com.aijobportal.job.entity.Job;
import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ClaudeAiClient implements AiClient {

    private static final Logger log = LoggerFactory.getLogger(ClaudeAiClient.class);

    private final AnthropicClient client;
    private final String model;
    private final ObjectMapper objectMapper;
    private final HeuristicAiClient heuristic;

    public ClaudeAiClient(String apiKey, String model, ObjectMapper objectMapper, HeuristicAiClient heuristic) {
        this.client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
        this.model = model == null || model.isBlank() ? "claude-sonnet-4-6" : model;
        this.objectMapper = objectMapper;
        this.heuristic = heuristic;
    }

    @Override
    public ResumeAnalysisResponse analyzeResume(
            CandidateProfileResponse profile,
            ResumeAnalysisResponse current,
            String resumeText
    ) {
        if (resumeText == null || resumeText.isBlank()) {
            throw new IllegalStateException("Resume text is required for Claude analysis.");
        }
        String raw = complete(
                """
                Extract a structured professional profile from the resume text.
                Return JSON only with this exact shape:
                {
                  "skills": ["string"],
                  "additionalSkills": ["string"],
                  "experience": "string",
                  "titles": ["string"],
                  "education": ["string"],
                  "certifications": ["string"],
                  "seniority": "string",
                  "technologies": ["string"],
                  "projects": ["string"],
                  "industries": ["string"]
                }
                Use empty arrays or empty strings when unknown. Do not invent employers or degrees that are not in the text.
                """,
                "Resume text:\n" + resumeText
        );
        return parseAnalysis(raw, current);
    }

    @Override
    public MatchResultResponse scoreJob(Job job, CandidateProfileResponse profile) {
        return heuristic.scoreJob(job, profile);
    }

    @Override
    public List<MatchResultResponse> rankJobs(CandidateProfileResponse profile, List<Job> jobs) {
        if (jobs == null || jobs.isEmpty()) {
            return List.of();
        }
        String raw = complete(
                """
                Rank the candidate against the supplied jobs. Return JSON only:
                {
                  "matches": [
                    {
                      "jobId": "string",
                      "matchScore": 0,
                      "strongAreas": ["string"],
                      "gaps": ["string"],
                      "explanation": "string"
                    }
                  ]
                }
                Include every jobId exactly once. matchScore is an integer from 0 to 98.
                explanation is brief decision support, not a hiring decision.
                """,
                "Candidate:\n" + profileJson(profile) + "\n\nJobs:\n" + jobsJson(jobs)
        );
        return parseRankings(raw, profile, jobs);
    }

    @Override
    public InterviewQuestionsResponse interviewQuestions(Job job, CandidateProfileResponse candidate) {
        return heuristic.interviewQuestions(job, candidate);
    }

    static ResumeAnalysisResponse parseAnalysis(ObjectMapper mapper, String raw, ResumeAnalysisResponse current) {
        JsonNode root = readJson(mapper, raw);
        return new ResumeAnalysisResponse(
                ResumeStatus.COMPLETE.name(),
                current == null ? null : current.fileName(),
                current == null ? null : current.uploadedAt(),
                strings(root, "skills"),
                strings(root, "additionalSkills"),
                text(root, "experience"),
                strings(root, "titles"),
                strings(root, "education"),
                strings(root, "certifications"),
                text(root, "seniority"),
                strings(root, "technologies"),
                strings(root, "projects"),
                strings(root, "industries"),
                null
        );
    }

    static List<MatchResultResponse> parseRankings(
            ObjectMapper mapper,
            String raw,
            CandidateProfileResponse profile,
            List<Job> jobs
    ) {
        JsonNode root = readJson(mapper, raw);
        JsonNode matches = root.path("matches");
        Map<String, MatchResultResponse> byId = new LinkedHashMap<>();
        if (matches.isArray()) {
            for (JsonNode item : matches) {
                String jobId = text(item, "jobId");
                if (jobId == null) {
                    continue;
                }
                byId.put(jobId, new MatchResultResponse(
                        jobId,
                        profile == null ? "guest" : profile.id(),
                        clampScore(item.path("matchScore").asInt(0)),
                        strings(item, "strongAreas"),
                        strings(item, "gaps"),
                        text(item, "explanation")
                ));
            }
        }
        List<MatchResultResponse> ordered = new ArrayList<>();
        for (Job job : jobs) {
            MatchResultResponse match = byId.get(job.getId());
            if (match != null) {
                ordered.add(match);
            }
        }
        return ordered;
    }

    ResumeAnalysisResponse parseAnalysis(String raw, ResumeAnalysisResponse current) {
        return parseAnalysis(objectMapper, raw, current);
    }

    List<MatchResultResponse> parseRankings(String raw, CandidateProfileResponse profile, List<Job> jobs) {
        return parseRankings(objectMapper, raw, profile, jobs);
    }

    private String complete(String system, String user) {
        MessageCreateParams params = MessageCreateParams.builder()
                .model(model)
                .maxTokens(2048L)
                .system(system)
                .addUserMessage(user)
                .build();
        Message message = client.messages().create(params);
        String text = message.content().stream()
                .flatMap(block -> block.text().stream())
                .map(block -> block.text())
                .reduce("", String::concat);
        if (text.isBlank()) {
            throw new IllegalStateException("Claude returned an empty response.");
        }
        log.info("Claude completed a request using model {}", model);
        return text;
    }

    private String profileJson(CandidateProfileResponse profile) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "id", profile.id(),
                    "title", nullToEmpty(profile.title()),
                    "experience", nullToEmpty(profile.experience()),
                    "location", nullToEmpty(profile.location()),
                    "skills", profile.skills() == null ? List.of() : profile.skills(),
                    "education", profile.education() == null ? List.of() : profile.education()
            ));
        } catch (Exception ex) {
            throw new IllegalStateException("Could not serialize candidate profile.", ex);
        }
    }

    private String jobsJson(List<Job> jobs) {
        List<Map<String, Object>> payload = new ArrayList<>();
        for (Job job : jobs) {
            String description = job.getDescription() == null ? "" : job.getDescription();
            if (description.length() > 280) {
                description = description.substring(0, 280);
            }
            payload.add(Map.of(
                    "jobId", job.getId(),
                    "role", nullToEmpty(job.getRole()),
                    "skills", job.getSkills() == null ? List.of() : job.getSkills(),
                    "location", nullToEmpty(job.getLocation()),
                    "experience", nullToEmpty(job.getExperience()),
                    "description", description
            ));
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not serialize jobs.", ex);
        }
    }

    static JsonNode readJson(ObjectMapper mapper, String raw) {
        String trimmed = raw.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            trimmed = trimmed.substring(start, end + 1);
        }
        try {
            return mapper.readTree(trimmed);
        } catch (Exception ex) {
            throw new IllegalStateException("Claude returned invalid JSON.", ex);
        }
    }

    private static List<String> strings(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (!value.isArray()) {
            return List.of();
        }
        List<String> items = new ArrayList<>();
        value.forEach(item -> {
            String text = item.asText("").trim();
            if (!text.isBlank()) {
                items.add(text);
            }
        });
        return items;
    }

    private static String text(JsonNode node, String field) {
        String value = node.path(field).asText("").trim();
        return value.isBlank() ? null : value;
    }

    private static int clampScore(int score) {
        return Math.max(0, Math.min(98, score));
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
