package com.aijobportal.candidate.dto;

import java.util.List;

public record CandidateProfileResponse(
        String id,
        String fullName,
        String email,
        String phone,
        String location,
        String title,
        String experience,
        List<String> skills,
        List<String> education,
        List<String> certifications,
        String portfolioUrl,
        String linkedinUrl
) {
}
