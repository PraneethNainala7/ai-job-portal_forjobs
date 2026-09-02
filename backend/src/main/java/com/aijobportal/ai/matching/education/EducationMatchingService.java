package com.aijobportal.ai.matching.education;

import com.aijobportal.ai.matching.model.NormalizedCandidateProfile;
import com.aijobportal.ai.matching.model.NormalizedJobRequirements;
import com.aijobportal.ai.matching.policy.MatchScorePolicy;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class EducationMatchingService {

    private final MatchScorePolicy policy;

    public EducationMatchingService(MatchScorePolicy policy) {
        this.policy = policy;
    }

    public int score(NormalizedCandidateProfile candidate, NormalizedJobRequirements job) {
        int maxPoints = policy.getWeights().getEducation();
        String description = job.description();
        if (description == null || description.isBlank()) {
            return 0;
        }
        String text = description.toLowerCase(Locale.ROOT);
        int hits = 0;
        for (String item : candidate.education()) {
            if (item != null && text.contains(item.toLowerCase(Locale.ROOT))) {
                hits++;
            }
        }
        for (String item : candidate.certifications()) {
            if (item != null && text.contains(item.toLowerCase(Locale.ROOT))) {
                hits++;
            }
        }
        return Math.min(maxPoints, hits * 2);
    }
}
