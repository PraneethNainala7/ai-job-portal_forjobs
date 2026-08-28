package com.aijobportal.ai.service;

import com.aijobportal.ai.entity.AiUsage;
import com.aijobportal.ai.repository.AiUsageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class AiUsageService {

    private static final String GLOBAL = "global";
    private final AiUsageRepository aiUsageRepository;

    public AiUsageService(AiUsageRepository aiUsageRepository) {
        this.aiUsageRepository = aiUsageRepository;
    }

    @Transactional
    public void increment(String key) {
        AiUsage usage = aiUsageRepository.findById(GLOBAL).orElseGet(() -> {
            AiUsage created = new AiUsage();
            created.setId(GLOBAL);
            return created;
        });
        switch (key) {
            case "resumeAnalysis" -> usage.setResumeAnalysis(usage.getResumeAnalysis() + 1);
            case "jobMatch" -> usage.setJobMatch(usage.getJobMatch() + 1);
            case "recommendations" -> usage.setRecommendations(usage.getRecommendations() + 1);
            case "interviewQuestions" -> usage.setInterviewQuestions(usage.getInterviewQuestions() + 1);
            case "failures" -> usage.setFailures(usage.getFailures() + 1);
            default -> {
            }
        }
        aiUsageRepository.save(usage);
    }

    @Transactional(readOnly = true)
    public Map<String, Integer> snapshot() {
        AiUsage usage = aiUsageRepository.findById(GLOBAL).orElseGet(AiUsage::new);
        return Map.of(
                "resumeAnalysis", usage.getResumeAnalysis(),
                "jobMatch", usage.getJobMatch(),
                "recommendations", usage.getRecommendations(),
                "interviewQuestions", usage.getInterviewQuestions(),
                "failures", usage.getFailures()
        );
    }
}
