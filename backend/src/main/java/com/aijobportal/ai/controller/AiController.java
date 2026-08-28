package com.aijobportal.ai.controller;

import com.aijobportal.ai.dto.InterviewQuestionsEnvelope;
import com.aijobportal.ai.dto.InterviewQuestionsRequest;
import com.aijobportal.ai.dto.MatchResultResponse;
import com.aijobportal.ai.dto.RecommendationResponse;
import com.aijobportal.ai.service.AiService;
import com.aijobportal.candidate.dto.ResumeEnvelope;
import com.aijobportal.config.security.SecurityUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/resume/analyze")
    public ResumeEnvelope analyzeResume() {
        return new ResumeEnvelope(aiService.analyzeResume(SecurityUtils.requireUser()));
    }

    @PostMapping("/jobs/recommend")
    public RecommendationResponse recommend() {
        return new RecommendationResponse(aiService.recommend(SecurityUtils.requireUser()).items());
    }

    @PostMapping("/jobs/{id}/match")
    public MatchResultResponse match(@PathVariable String id) {
        return aiService.match(SecurityUtils.requireUser(), id);
    }

    @PostMapping("/interview-questions")
    public InterviewQuestionsEnvelope interviewQuestions(@RequestBody InterviewQuestionsRequest request) {
        return aiService.interviewQuestions(SecurityUtils.requireUser(), request);
    }
}
