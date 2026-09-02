package com.aijobportal.ai.dto;

import java.util.List;

public record RecommendationResponse(
        List<RecommendationItem> items,
        int minScore,
        int evaluatedCount,
        boolean resumeReady
) {
    public RecommendationResponse(List<RecommendationItem> items) {
        this(items, 60, 0, false);
    }
}
