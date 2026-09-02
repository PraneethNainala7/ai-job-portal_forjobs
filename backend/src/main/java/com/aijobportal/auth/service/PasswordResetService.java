package com.aijobportal.auth.service;

import com.aijobportal.auth.entity.PasswordResetToken;
import com.aijobportal.auth.entity.User;
import com.aijobportal.auth.repository.PasswordResetTokenRepository;
import com.aijobportal.auth.repository.UserRepository;
import com.aijobportal.common.exception.ApiException;
import com.aijobportal.notification.config.MailProperties;
import com.aijobportal.notification.event.PasswordResetRequestedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class PasswordResetService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final MailProperties mailProperties;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            ApplicationEventPublisher eventPublisher,
            MailProperties mailProperties
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
        this.mailProperties = mailProperties;
    }

    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmailIgnoreCase(email.trim()).ifPresent(user -> {
            if (user.getAccountStatus() == com.aijobportal.common.domain.AccountStatus.INACTIVE) {
                return;
            }
            String rawToken = generateRawToken();
            tokenRepository.invalidateUnusedForUser(user.getId(), Instant.now());

            PasswordResetToken token = new PasswordResetToken();
            token.setUser(user);
            token.setTokenHash(hashToken(rawToken));
            token.setExpiresAt(Instant.now().plusSeconds(expirationSeconds()));
            tokenRepository.save(token);

            String resetLink = buildResetLink(rawToken);
            eventPublisher.publishEvent(new PasswordResetRequestedEvent(user.getEmail(), user.getName(), resetLink));
        });
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        if (rawToken == null || rawToken.isBlank()) {
            throw ApiException.badRequest("Reset token is required.");
        }
        PasswordResetToken token = tokenRepository.findByTokenHashAndUsedAtIsNull(hashToken(rawToken.trim()))
                .orElseThrow(() -> ApiException.badRequest("This reset link is invalid or has expired."));
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw ApiException.badRequest("This reset link is invalid or has expired.");
        }
        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        token.setUsedAt(Instant.now());
        tokenRepository.invalidateUnusedForUser(user.getId(), Instant.now());
    }

    private long expirationSeconds() {
        return mailProperties.getPasswordReset().getExpirationMinutes() * 60L;
    }

    private String buildResetLink(String rawToken) {
        String base = mailProperties.getFrontendUrl();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/reset-password?token=" + rawToken;
    }

    static String generateRawToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
