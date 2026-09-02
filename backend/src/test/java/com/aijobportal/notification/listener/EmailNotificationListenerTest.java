package com.aijobportal.notification.listener;

import com.aijobportal.notification.email.EmailService;
import com.aijobportal.notification.email.EmailTemplateService;
import com.aijobportal.notification.email.dto.EmailMessage;
import com.aijobportal.notification.event.EmployerRegisteredEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailNotificationListenerTest {

    @Mock
    private EmailTemplateService templateService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailNotificationListener listener;

    @Test
    void onEmployerRegisteredSendsEmail() {
        EmployerRegisteredEvent event = new EmployerRegisteredEvent("e@demo.com", "Alex", "Acme");
        EmailMessage message = new EmailMessage("e@demo.com", "Subject", "Body");
        when(templateService.buildEmployerRegistered(event)).thenReturn(message);

        listener.onEmployerRegistered(event);

        verify(emailService).send(message, "EMPLOYER_REGISTERED");
    }
}
