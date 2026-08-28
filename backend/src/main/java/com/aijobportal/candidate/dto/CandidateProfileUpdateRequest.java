package com.aijobportal.candidate.dto;

import java.util.List;

public record CandidateProfileUpdateRequest(
        String fullName,
        String phone,
        String location,
        String title,
        String experience,
        List<String> skills,
        List<String> education,
        List<String> certifications,
        String linkedinUrl,
        String portfolioUrl
) {
}
