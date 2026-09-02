package com.aijobportal.notification.email;

import com.aijobportal.notification.config.MailProperties;
import com.aijobportal.notification.event.EmployerRejectedEvent;
import com.aijobportal.notification.event.EmployerRegisteredEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailTemplateServiceTest {

    private EmailTemplateService templateService;

    @BeforeEach
    void setUp() {
        MailProperties properties = new MailProperties();
        properties.setFrontendUrl("http://localhost:3000");
        templateService = new EmailTemplateService(new EmailLayout(), properties);
    }

    @Test
    void employerRegisteredIncludesLoginUrl() {
        var message = templateService.buildEmployerRegistered(
                new EmployerRegisteredEvent("employer@demo.com", "Alex", "Acme Corp"));
        assertEquals("employer@demo.com", message.to());
        assertEquals("Registration received — Acme Corp", message.subject());
        assertTrue(message.textBody().contains("http://localhost:3000/login"));
        assertTrue(message.textBody().contains("Alex"));
    }

    @Test
    void employerRejectedUsesDefaultReasonWhenMissing() {
        var message = templateService.buildEmployerRejected(
                new EmployerRejectedEvent("employer@demo.com", "Alex", "Acme Corp", null));
        assertTrue(message.textBody().contains("No additional details were provided."));
    }
}
