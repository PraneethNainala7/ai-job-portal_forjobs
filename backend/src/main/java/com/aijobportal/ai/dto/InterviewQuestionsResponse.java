package com.aijobportal.ai.dto;

import java.util.List;

public record InterviewQuestionsResponse(
        List<String> technical,
        List<String> resumeBased,
        List<String> jobSpecific,
        List<String> behavioral
) {
}
