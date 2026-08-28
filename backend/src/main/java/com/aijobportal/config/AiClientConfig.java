package com.aijobportal.config;

import com.aijobportal.ai.client.AiClient;
import com.aijobportal.ai.client.ClaudeAiClient;
import com.aijobportal.ai.client.DelegatingAiClient;
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
        if (apiKey == null || apiKey.isBlank()) {
            log.info("ANTHROPIC_API_KEY is not set; job ranking and Claude resume analysis are disabled.");
            return heuristicAiClient;
        }
        log.info("Using Claude for resume analysis and job ranking.");
        return new DelegatingAiClient(
                new ClaudeAiClient(apiKey, model, objectMapper, heuristicAiClient),
                heuristicAiClient
        );
    }
}
