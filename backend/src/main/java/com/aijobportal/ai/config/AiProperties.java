package com.aijobportal.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    private int recommendationMinScore = 60;
    private int recommendationMaxResults = 12;
    private int recommendationBatchSize = 20;

    public int getRecommendationMinScore() {
        return recommendationMinScore;
    }

    public void setRecommendationMinScore(int recommendationMinScore) {
        this.recommendationMinScore = recommendationMinScore;
    }

    public int getRecommendationMaxResults() {
        return recommendationMaxResults;
    }

    public void setRecommendationMaxResults(int recommendationMaxResults) {
        this.recommendationMaxResults = recommendationMaxResults;
    }

    public int getRecommendationBatchSize() {
        return recommendationBatchSize;
    }

    public void setRecommendationBatchSize(int recommendationBatchSize) {
        this.recommendationBatchSize = recommendationBatchSize;
    }
}
