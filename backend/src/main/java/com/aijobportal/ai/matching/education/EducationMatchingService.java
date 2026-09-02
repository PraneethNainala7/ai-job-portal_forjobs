package com.aijobportal.ai.matching.education;

import com.aijobportal.ai.matching.model.NormalizedCandidateProfile;
import com.aijobportal.ai.matching.model.NormalizedJobRequirements;
import com.aijobportal.ai.matching.policy.MatchScorePolicy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class EducationMatchingService {

    private final MatchScorePolicy policy;

    public EducationMatchingService(MatchScorePolicy policy) {
        this.policy = policy;
    }

    public int score(NormalizedCandidateProfile candidate, NormalizedJobRequirements job) {
        int maxPoints = policy.getWeights().getEducation();
        List<String> educationRequirements = job.educationRequirements();
        List<String> certificationRequirements = job.certificationRequirements();

        if (!educationRequirements.isEmpty() || !certificationRequirements.isEmpty()) {
            int totalRequirements = educationRequirements.size() + certificationRequirements.size();
            int hits = countStructuredMatches(educationRequirements, candidate.education())
                    + countStructuredMatches(certificationRequirements, candidate.certifications());
            return (int) Math.round(maxPoints * ((double) hits / totalRequirements));
        }
        return 0;
    }

    public static boolean isApplicable(NormalizedJobRequirements job) {
        List<String> educationRequirements = job.educationRequirements();
        List<String> certificationRequirements = job.certificationRequirements();
        return !educationRequirements.isEmpty() || !certificationRequirements.isEmpty();
    }

    private static int countStructuredMatches(List<String> requirements, List<String> candidateValues) {
        if (requirements.isEmpty() || candidateValues.isEmpty()) {
            return 0;
        }
        int hits = 0;
        for (String requirement : requirements) {
            if (requirement == null || requirement.isBlank()) {
                continue;
            }
            boolean matched = candidateValues.stream()
                    .anyMatch(candidate -> EducationTextMatcher.matches(requirement, candidate));
            if (matched) {
                hits++;
            }
        }
        return hits;
    }
}
