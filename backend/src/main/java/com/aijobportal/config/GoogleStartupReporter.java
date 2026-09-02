package com.aijobportal.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class GoogleStartupReporter {

    private static final Logger log = LoggerFactory.getLogger(GoogleStartupReporter.class);

    private final GoogleProperties googleProperties;

    public GoogleStartupReporter(GoogleProperties googleProperties) {
        this.googleProperties = googleProperties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void reportGoogleStatus() {
        if (googleProperties.isConfigured()) {
            log.info(
                    "Google sign-in enabled (client ID suffix: ...{}).",
                    suffix(googleProperties.getClientId())
            );
            return;
        }
        log.warn(
                "Google sign-in disabled. Set GOOGLE_CLIENT_ID on the Spring Boot process before mvnw spring-boot:run."
        );
    }

    private static String suffix(String clientId) {
        if (clientId == null || clientId.length() < 12) {
            return "****";
        }
        return clientId.substring(clientId.length() - 12);
    }
}
