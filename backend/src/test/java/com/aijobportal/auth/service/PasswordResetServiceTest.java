package com.aijobportal.auth.service;

import com.aijobportal.auth.entity.PasswordResetToken;
import com.aijobportal.auth.entity.User;
import com.aijobportal.auth.repository.PasswordResetTokenRepository;
import com.aijobportal.auth.repository.UserRepository;
import com.aijobportal.common.domain.AccountStatus;
import com.aijobportal.common.domain.Role;
import com.aijobportal.common.exception.ApiException;
import com.aijobportal.notification.config.MailProperties;
import com.aijobportal.notification.event.PasswordResetRequestedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        MailProperties properties = new MailProperties();
        properties.setFrontendUrl("http://localhost:3000");
        passwordResetService = new PasswordResetService(
                userRepository, tokenRepository, passwordEncoder, eventPublisher, properties);
    }

    @Test
    void requestResetPublishesEventForActiveUser() {
        User user = activeUser();
        when(userRepository.findByEmailIgnoreCase("user@demo.com")).thenReturn(Optional.of(user));

        passwordResetService.requestReset("user@demo.com");

        verify(tokenRepository).invalidateUnusedForUser(eq("user-1"), any(Instant.class));
        verify(tokenRepository).save(any(PasswordResetToken.class));
        ArgumentCaptor<PasswordResetRequestedEvent> captor = ArgumentCaptor.forClass(PasswordResetRequestedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals("user@demo.com", captor.getValue().email());
        assertTrueResetLink(captor.getValue().resetLink());
    }

    @Test
    void requestResetDoesNothingForUnknownEmail() {
        when(userRepository.findByEmailIgnoreCase("missing@demo.com")).thenReturn(Optional.empty());

        passwordResetService.requestReset("missing@demo.com");

        verify(tokenRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void resetPasswordUpdatesHashAndMarksTokenUsed() {
        String rawToken = PasswordResetService.generateRawToken();
        User user = activeUser();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        when(tokenRepository.findByTokenHashAndUsedAtIsNull(PasswordResetService.hashToken(rawToken)))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.encode("new-password-123")).thenReturn("encoded");

        passwordResetService.resetPassword(rawToken, "new-password-123");

        assertEquals("encoded", user.getPasswordHash());
        verify(tokenRepository).invalidateUnusedForUser(eq("user-1"), any(Instant.class));
    }

    @Test
    void resetPasswordRejectsExpiredToken() {
        String rawToken = PasswordResetService.generateRawToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(activeUser());
        token.setExpiresAt(Instant.now().minusSeconds(60));
        when(tokenRepository.findByTokenHashAndUsedAtIsNull(PasswordResetService.hashToken(rawToken)))
                .thenReturn(Optional.of(token));

        assertThrows(ApiException.class, () -> passwordResetService.resetPassword(rawToken, "new-password-123"));
    }

    private static User activeUser() {
        User user = new User();
        user.setId("user-1");
        user.setName("Alex");
        user.setEmail("user@demo.com");
        user.setRole(Role.CANDIDATE);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setPasswordHash("old");
        return user;
    }

    private static void assertTrueResetLink(String resetLink) {
        assertEquals(true, resetLink.startsWith("http://localhost:3000/reset-password?token="));
    }
}
