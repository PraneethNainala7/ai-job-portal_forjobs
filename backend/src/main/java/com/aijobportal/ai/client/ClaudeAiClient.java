package com.aijobportal.ai.client;

import com.aijobportal.ai.dto.InterviewQuestionsResponse;
import com.aijobportal.candidate.dto.CandidateProfileResponse;
import com.aijobportal.candidate.dto.ResumeAnalysisResponse;
import com.aijobportal.candidate.mapper.CandidateMapper;
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
import java.util.HashMap;
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
    public ResumeAnalysisOutcome analyzeResume(
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
                  "industries": ["string"],
                  "programmingLanguages": ["string"],
                  "frameworks": ["string"],
                  "databases": ["string"],
                  "cloudTechnologies": ["string"],
                  "tools": ["string"],
                  "experienceYears": { "total": 0, "backend": 0, "frontend": 0 }
                }
                Use empty arrays, empty strings, or zero for unknown values.
                Do not invent employers or degrees that are not in the text.
                Do not list Java if the resume only mentions JavaScript. Treat Java and JavaScript as different skills.
                Only include skills with evidence in the resume text.
                """,
                "Resume text:\n" + resumeText
        );
        JsonNode root = readJson(objectMapper, raw);
        ResumeAnalysisResponse response = parseAnalysis(root, current);
        return new ResumeAnalysisOutcome(response, parsedData(root, response));
    }

    @Override
    public InterviewQuestionsResponse interviewQuestions(Job job, CandidateProfileResponse candidate) {
        return heuristic.interviewQuestions(job, candidate);
    }

    static ResumeAnalysisResponse parseAnalysis(ObjectMapper mapper, String raw, ResumeAnalysisResponse current) {
        return parseAnalysis(readJson(mapper, raw), current);
    }

    static ResumeAnalysisResponse parseAnalysis(JsonNode root, ResumeAnalysisResponse current) {
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

    static Map<String, Object> parsedData(JsonNode root, ResumeAnalysisResponse response) {
        Map<String, Object> data = new LinkedHashMap<>(CandidateMapper.toParsedData(response));
        data.put("programmingLanguages", strings(root, "programmingLanguages"));
        data.put("frameworks", strings(root, "frameworks"));
        data.put("databases", strings(root, "databases"));
        data.put("cloudTechnologies", strings(root, "cloudTechnologies"));
        data.put("tools", strings(root, "tools"));
        data.put("experienceYears", experienceYears(root));
        return data;
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

    private static Map<String, Integer> experienceYears(JsonNode root) {
        JsonNode node = root.path("experienceYears");
        if (!node.isObject()) {
            return Map.of();
        }
        Map<String, Integer> values = new HashMap<>();
        node.fields().forEachRemaining(entry -> {
            int years = entry.getValue().asInt(0);
            if (years > 0) {
                values.put(entry.getKey(), years);
            }
        });
        return values;
    }
}
