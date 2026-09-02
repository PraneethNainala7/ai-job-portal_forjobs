package com.aijobportal.ai.matching.policy;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MatchScorePolicy.class)
public class MatchScorePolicyConfig {
}
