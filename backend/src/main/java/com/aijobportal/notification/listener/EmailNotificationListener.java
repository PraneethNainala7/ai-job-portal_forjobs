package com.aijobportal.notification.listener;

import com.aijobportal.notification.email.EmailService;
import com.aijobportal.notification.email.EmailTemplateService;
import com.aijobportal.notification.email.EmailTemplateType;
import com.aijobportal.notification.event.ApplicationSubmittedEvent;
import com.aijobportal.notification.event.CandidateAccountHeldEvent;
import com.aijobportal.notification.event.CandidateAccountRestoredEvent;
import com.aijobportal.notification.event.CandidateRejectedEvent;
import com.aijobportal.notification.event.CandidateShortlistedEvent;
import com.aijobportal.notification.event.EmployerAccountHeldEvent;
import com.aijobportal.notification.event.EmployerAccountRestoredEvent;
import com.aijobportal.notification.event.EmployerApprovedEvent;
import com.aijobportal.notification.event.EmployerRegisteredEvent;
import com.aijobportal.notification.event.EmployerRejectedEvent;
import com.aijobportal.notification.email.dto.EmailMessage;
import com.aijobportal.notification.event.PasswordResetRequestedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class EmailNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationListener.class);

    private final EmailTemplateService templateService;
    private final EmailService emailService;

    public EmailNotificationListener(EmailTemplateService templateService, EmailService emailService) {
        this.templateService = templateService;
        this.emailService = emailService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEmployerRegistered(EmployerRegisteredEvent event) {
        dispatch(EmailTemplateType.EMPLOYER_REGISTERED, templateService.buildEmployerRegistered(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEmployerApproved(EmployerApprovedEvent event) {
        dispatch(EmailTemplateType.EMPLOYER_APPROVED, templateService.buildEmployerApproved(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEmployerRejected(EmployerRejectedEvent event) {
        dispatch(EmailTemplateType.EMPLOYER_REJECTED, templateService.buildEmployerRejected(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEmployerHeld(EmployerAccountHeldEvent event) {
        dispatch(EmailTemplateType.EMPLOYER_HELD, templateService.buildEmployerHeld(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEmployerRestored(EmployerAccountRestoredEvent event) {
        dispatch(EmailTemplateType.EMPLOYER_RESTORED, templateService.buildEmployerRestored(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCandidateHeld(CandidateAccountHeldEvent event) {
        dispatch(EmailTemplateType.CANDIDATE_HELD, templateService.buildCandidateHeld(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCandidateRestored(CandidateAccountRestoredEvent event) {
        dispatch(EmailTemplateType.CANDIDATE_RESTORED, templateService.buildCandidateRestored(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApplicationSubmitted(ApplicationSubmittedEvent event) {
        dispatch(EmailTemplateType.APPLICATION_SUBMITTED, templateService.buildApplicationSubmitted(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCandidateShortlisted(CandidateShortlistedEvent event) {
        dispatch(EmailTemplateType.CANDIDATE_SHORTLISTED, templateService.buildCandidateShortlisted(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCandidateRejected(CandidateRejectedEvent event) {
        dispatch(EmailTemplateType.CANDIDATE_REJECTED, templateService.buildCandidateRejected(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPasswordResetRequested(PasswordResetRequestedEvent event) {
        dispatch(EmailTemplateType.PASSWORD_RESET, templateService.buildPasswordReset(event));
    }

    private void dispatch(EmailTemplateType templateType, EmailMessage message) {
        log.info("Email notification triggered: template={}", templateType.name());
        emailService.send(message, templateType.name());
    }
}
