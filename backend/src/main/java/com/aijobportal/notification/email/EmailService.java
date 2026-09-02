package com.aijobportal.notification.email;

import com.aijobportal.notification.email.dto.EmailMessage;

public interface EmailService {

    void send(EmailMessage message, String templateType);
}
