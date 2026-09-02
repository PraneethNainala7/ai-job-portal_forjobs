package com.aijobportal.notification.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MailStartupReporter {

    private static final Logger log = LoggerFactory.getLogger(MailStartupReporter.class);

    private final MailProperties mailProperties;
    private final String mailUsername;
    private final String mailPassword;

    public MailStartupReporter(
            MailProperties mailProperties,
            @Value("${spring.mail.username:}") String mailUsername,
            @Value("${spring.mail.password:}") String mailPassword
    ) {
        this.mailProperties = mailProperties;
        this.mailUsername = mailUsername;
        this.mailPassword = mailPassword;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void reportMailStatus() {
        boolean enabled = mailProperties.getMail().isEnabled();
        boolean usernameConfigured = mailUsername != null && !mailUsername.isBlank();
        boolean passwordConfigured = mailPassword != null && !mailPassword.isBlank();

        if (enabled && usernameConfigured && passwordConfigured) {
            log.info(
                    "Email notifications enabled (SMTP user: {}). Gmail App Password must be set via MAIL_APP_PASSWORD on the Spring Boot process.",
                    maskUsername(mailUsername)
            );
            return;
        }

        if (enabled) {
            log.warn(
                    "Email notifications enabled but SMTP credentials are incomplete (MAIL_USERNAME configured={}, MAIL_APP_PASSWORD configured={}). Emails will fail until both are set on the Spring Boot process.",
                    usernameConfigured,
                    passwordConfigured
            );
            return;
        }

        log.info(
                "Email notifications disabled (app.mail.enabled=false). Set MAIL_ENABLED=true, MAIL_USERNAME, and MAIL_APP_PASSWORD on the Spring Boot process before mvnw spring-boot:run to send mail."
        );
    }

    private static String maskUsername(String username) {
        if (username == null || !username.contains("@")) {
            return "***";
        }
        int at = username.indexOf('@');
        if (at <= 1) {
            return "***" + username.substring(at);
        }
        return username.charAt(0) + "***" + username.substring(at);
    }
}
