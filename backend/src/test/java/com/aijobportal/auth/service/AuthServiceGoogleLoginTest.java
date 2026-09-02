package com.aijobportal.auth.service;

import com.aijobportal.auth.dto.LoginRequest;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceGoogleLoginTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private CandidateProfileRepository candidateProfileRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                companyRepository,
                candidateProfileRepository,
                passwordEncoder,
                eventPublisher
        );
    }

    @Test
    void loginRejectsGoogleOnlyAccount() {
        User user = new User();
        user.setEmail("user@demo.com");
        user.setPasswordHash(null);
        user.setRole(Role.CANDIDATE);
        user.setAccountStatus(AccountStatus.ACTIVE);
        when(userRepository.findByEmailIgnoreCase("user@demo.com")).thenReturn(Optional.of(user));

        ApiException ex = assertThrows(
                ApiException.class,
                () -> authService.login(new LoginRequest("user@demo.com", "secret"))
        );

        assertEquals("This account uses Google sign-in.", ex.getMessage());
    }
}
