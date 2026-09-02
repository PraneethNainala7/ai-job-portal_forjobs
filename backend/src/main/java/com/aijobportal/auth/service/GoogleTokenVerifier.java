package com.aijobportal.auth.service;

import com.aijobportal.config.GoogleProperties;
import com.aijobportal.common.exception.ApiException;
import com.google.api.client.json.webtoken.JsonWebSignature;
import com.google.auth.oauth2.TokenVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GoogleTokenVerifier {

    private static final Logger log = LoggerFactory.getLogger(GoogleTokenVerifier.class);

    private static final List<String> GOOGLE_ISSUERS = List.of(
            "https://accounts.google.com",
            "accounts.google.com"
    );

    private final GoogleProperties googleProperties;

    public GoogleTokenVerifier(GoogleProperties googleProperties) {
        this.googleProperties = googleProperties;
    }

    public GoogleUserInfo verify(String idToken) {
        if (!googleProperties.isConfigured()) {
            throw ApiException.unavailable("Google sign-in is not configured.");
        }
        if (idToken == null || idToken.isBlank()) {
            throw ApiException.unauthorized("Google sign-in token is missing.");
        }
        try {
            JsonWebSignature signature = verifySignature(idToken.trim());
            JsonWebSignature.Payload payload = signature.getPayload();
            String sub = payload.getSubject();
            String email = (String) payload.get("email");
            String name = (String) payload.get("name");
            if (sub == null || sub.isBlank() || email == null || email.isBlank()) {
                throw ApiException.unauthorized("Google sign-in is missing required profile details.");
            }
            if (!isEmailVerified(payload.get("email_verified"))) {
                throw ApiException.unauthorized("Verify your Google email address before signing in.");
            }
            return new GoogleUserInfo(sub, email.toLowerCase(), blankToDefault(name, email), true);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Google ID token verification failed for audience {}: {}", googleProperties.getClientId(), ex.getMessage());
            throw ApiException.unauthorized("Google sign-in could not be verified.");
        }
    }

    static boolean isEmailVerified(Object value) {
        if (value instanceof Boolean verified) {
            return verified;
        }
        if (value instanceof String verified) {
            return "true".equalsIgnoreCase(verified.trim());
        }
        return false;
    }

    private JsonWebSignature verifySignature(String idToken) throws Exception {
        Exception lastFailure = null;
        for (String issuer : GOOGLE_ISSUERS) {
            try {
                TokenVerifier verifier = TokenVerifier.newBuilder()
                        .setAudience(googleProperties.getClientId())
                        .setIssuer(issuer)
                        .build();
                return verifier.verify(idToken);
            } catch (Exception ex) {
                lastFailure = ex;
            }
        }
        if (lastFailure != null) {
            throw lastFailure;
        }
        throw new IllegalStateException("Google token verification failed.");
    }

    private static String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    public record GoogleUserInfo(String sub, String email, String name, boolean emailVerified) {
    }
}
