package com.aijobportal.notification.email;

import com.aijobportal.notification.email.dto.EmailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(DisabledEmailService.class);

    @Override
    public void send(EmailMessage message, String templateType) {
        log.info(
                "Email skipped (mail disabled): template={} recipient={}. Set MAIL_ENABLED=true on the Spring Boot process to send.",
                templateType,
                EmailServiceImpl.maskEmail(message.to())
        );
    }
}
