package com.aijobportal.auth.service;

import com.aijobportal.auth.entity.User;
import com.aijobportal.auth.repository.UserRepository;
import com.aijobportal.candidate.repository.CandidateProfileRepository;
import com.aijobportal.common.domain.AccountStatus;
import com.aijobportal.common.domain.Role;
import com.aijobportal.common.exception.ApiException;
import com.aijobportal.employer.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleAuthServiceTest {

    @Mock
    private GoogleTokenVerifier googleTokenVerifier;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CandidateProfileRepository candidateProfileRepository;
    @Mock
    private CompanyRepository companyRepository;

    private GoogleAuthService googleAuthService;

    @BeforeEach
    void setUp() {
        googleAuthService = new GoogleAuthService(
                googleTokenVerifier,
                userRepository,
                candidateProfileRepository,
                companyRepository
        );
    }

    @Test
    void autoLinksExistingEmailAccount() {
        GoogleTokenVerifier.GoogleUserInfo googleUser = googleProfile();
        User user = passwordUser();
        when(googleTokenVerifier.verify("token")).thenReturn(googleUser);
        when(userRepository.findByGoogleSub("google-sub-1")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("user@demo.com")).thenReturn(Optional.of(user));

        GoogleAuthService.GoogleAuthResult result = googleAuthService.authenticate("token", null);

        assertEquals("google-sub-1", user.getGoogleSub());
        verify(userRepository).save(user);
        assertFalse(result.needsEmployerOnboarding());
    }

    @Test
    void createsCandidateWhenRoleProvided() {
        GoogleTokenVerifier.GoogleUserInfo googleUser = googleProfile();
        when(googleTokenVerifier.verify("token")).thenReturn(googleUser);
        when(userRepository.findByGoogleSub("google-sub-1")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("user@demo.com")).thenReturn(Optional.empty());

        GoogleAuthService.GoogleAuthResult result = googleAuthService.authenticate("token", "CANDIDATE");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals(Role.CANDIDATE, saved.getRole());
        assertEquals(AccountStatus.ACTIVE, saved.getAccountStatus());
        assertEquals("google-sub-1", saved.getGoogleSub());
        verify(candidateProfileRepository).save(any());
        assertFalse(result.needsEmployerOnboarding());
    }

    @Test
    void rejectsEmployerSignupViaGoogle() {
        GoogleTokenVerifier.GoogleUserInfo googleUser = googleProfile();
        when(googleTokenVerifier.verify("token")).thenReturn(googleUser);
        when(userRepository.findByGoogleSub("google-sub-1")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("user@demo.com")).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> googleAuthService.authenticate("token", "EMPLOYER"));

        assertEquals("Employers must register with a company email and password.", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void allowsExistingEmployerGoogleLogin() {
        User employer = passwordUser();
        employer.setRole(Role.EMPLOYER);
        employer.setAccountStatus(AccountStatus.PENDING);
        employer.setGoogleSub("google-sub-1");
        when(googleTokenVerifier.verify("token")).thenReturn(googleProfile());
        when(userRepository.findByGoogleSub("google-sub-1")).thenReturn(Optional.of(employer));
        when(companyRepository.findByEmployerId("user-1")).thenReturn(Optional.empty());

        GoogleAuthService.GoogleAuthResult result = googleAuthService.authenticate("token", null);

        assertTrue(result.needsEmployerOnboarding());
        verify(userRepository, never()).save(any());
    }

    @Test
    void requiresRoleForNewUser() {
        GoogleTokenVerifier.GoogleUserInfo googleUser = googleProfile();
        when(googleTokenVerifier.verify("token")).thenReturn(googleUser);
        when(userRepository.findByGoogleSub("google-sub-1")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("user@demo.com")).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> googleAuthService.authenticate("token", null));

        assertEquals(GoogleAuthService.NEEDS_ROLE_CODE, ex.getCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void blocksAdminGoogleLogin() {
        User admin = passwordUser();
        admin.setRole(Role.ADMIN);
        when(googleTokenVerifier.verify("token")).thenReturn(googleProfile());
        when(userRepository.findByGoogleSub("google-sub-1")).thenReturn(Optional.of(admin));

        assertThrows(ApiException.class, () -> googleAuthService.authenticate("token", null));
    }

    private static GoogleTokenVerifier.GoogleUserInfo googleProfile() {
        return new GoogleTokenVerifier.GoogleUserInfo("google-sub-1", "user@demo.com", "Alex Candidate", true);
    }

    private static User passwordUser() {
        User user = new User();
        user.setId("user-1");
        user.setName("Alex Candidate");
        user.setEmail("user@demo.com");
        user.setRole(Role.CANDIDATE);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setPasswordHash("hashed");
        return user;
    }
}
