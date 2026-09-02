package com.aijobportal.candidate.service;

import com.aijobportal.auth.entity.User;
import com.aijobportal.candidate.dto.ResumeAnalysisResponse;
import com.aijobportal.candidate.entity.CandidateProfile;
import com.aijobportal.candidate.repository.CandidateProfileRepository;
import com.aijobportal.candidate.repository.ResumeRepository;
import com.aijobportal.auth.repository.UserRepository;
import com.aijobportal.common.domain.ResumeStatus;
import com.aijobportal.config.security.AuthPrincipal;
import com.aijobportal.common.domain.AccountStatus;
import com.aijobportal.common.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CandidateProfileServicePrefillTest {

    @Mock
    private CandidateProfileRepository profileRepository;
    @Mock
    private ResumeRepository resumeRepository;
    @Mock
    private UserRepository userRepository;

    private CandidateProfileService service;
    private CandidateProfile profile;
    private User user;

    @BeforeEach
    void setUp() {
        service = new CandidateProfileService(profileRepository, resumeRepository, userRepository, Path.of("uploads").toString());
        user = new User();
        user.setId("user-1");
        user.setName("Jane");
        user.setEmail("jane@example.com");
        user.setRole(Role.CANDIDATE);
        user.setAccountStatus(AccountStatus.ACTIVE);
        profile = new CandidateProfile();
        profile.setUser(user);
    }

    private AuthPrincipal principal() {
        when(profileRepository.findByUserId("user-1")).thenReturn(java.util.Optional.of(profile));
        return new AuthPrincipal("user-1", "jane@example.com", "Jane", Role.CANDIDATE, AccountStatus.ACTIVE);
    }

    @Test
    void prefillFillsBlankFieldsOnly() {
        ResumeAnalysisResponse analysis = new ResumeAnalysisResponse(
                ResumeStatus.COMPLETE.name(),
                "resume.pdf",
                null,
                List.of(),
                List.of(),
                "6 years",
                List.of("Backend Engineer"),
                List.of("B.Tech CS"),
                List.of("AWS Certified"),
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null
        );

        AuthPrincipal principal = principal();
        service.prefillFromResumeAnalysis(principal, analysis);

        assertEquals("Backend Engineer", profile.getTitle());
        assertEquals("6 years", profile.getExperience());
        assertEquals(List.of("B.Tech CS"), profile.getEducation());
        assertEquals(List.of("AWS Certified"), profile.getCertifications());
    }

    @Test
    void prefillDoesNotOverwriteExistingProfileValues() {
        profile.setTitle("Staff Engineer");
        profile.setExperience("8 years");
        profile.setEducation(List.of("M.Tech"));
        profile.setCertifications(List.of("PMP"));

        ResumeAnalysisResponse analysis = new ResumeAnalysisResponse(
                ResumeStatus.COMPLETE.name(),
                "resume.pdf",
                null,
                List.of(),
                List.of(),
                "6 years",
                List.of("Backend Engineer"),
                List.of("B.Tech CS"),
                List.of("AWS Certified"),
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null
        );

        AuthPrincipal principal = principal();
        service.prefillFromResumeAnalysis(principal, analysis);

        assertEquals("Staff Engineer", profile.getTitle());
        assertEquals("8 years", profile.getExperience());
        assertEquals(List.of("M.Tech"), profile.getEducation());
        assertEquals(List.of("PMP"), profile.getCertifications());
    }

    @Test
    void prefillSkipsFailedAnalysis() {
        service.prefillFromResumeAnalysis(
                new AuthPrincipal("user-1", "jane@example.com", "Jane", Role.CANDIDATE, AccountStatus.ACTIVE),
                new ResumeAnalysisResponse(
                ResumeStatus.FAILED.name(),
                "resume.pdf",
                null,
                List.of(),
                List.of(),
                "6 years",
                List.of("Backend Engineer"),
                List.of("B.Tech CS"),
                List.of("AWS Certified"),
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                "failed"
        ));

        assertTrue(profile.getTitle() == null || profile.getTitle().isBlank());
    }
}
