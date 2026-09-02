package com.aijobportal.config;

import com.aijobportal.ai.client.AiClient;
import com.aijobportal.ai.client.ClaudeAiClient;
import com.aijobportal.ai.client.DelegatingAiClient;
import com.aijobportal.ai.client.DisabledAiClient;
import com.aijobportal.ai.client.HeuristicAiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiClientConfig {

    private static final Logger log = LoggerFactory.getLogger(AiClientConfig.class);

    @Bean
    HeuristicAiClient heuristicAiClient() {
        return new HeuristicAiClient();
    }

    @Bean
    AiClient aiClient(
            HeuristicAiClient heuristicAiClient,
            ObjectMapper objectMapper,
            @Value("${app.anthropic.api-key:}") String apiKey,
            @Value("${app.anthropic.model:claude-sonnet-4-6}") String model
    ) {
        String cleanedKey = normalizeApiKey(apiKey);
        if (cleanedKey.isBlank()) {
            log.warn("ANTHROPIC_API_KEY is not set; resume analysis will return FAILED until configured.");
            return new DisabledAiClient(heuristicAiClient);
        }
        log.info("Using Claude for resume analysis.");
        ClaudeAiClient claude = new ClaudeAiClient(cleanedKey, model, objectMapper, heuristicAiClient);
        return new DelegatingAiClient(claude, heuristicAiClient);
    }

    private static String normalizeApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return "";
        }
        String cleanedKey = apiKey.trim();
        if (cleanedKey.startsWith("\"") && cleanedKey.endsWith("\"") && cleanedKey.length() > 1) {
            cleanedKey = cleanedKey.substring(1, cleanedKey.length() - 1);
        }
        return cleanedKey.trim();
    }
}
