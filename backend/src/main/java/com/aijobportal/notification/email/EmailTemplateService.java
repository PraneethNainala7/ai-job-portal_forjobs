package com.aijobportal.notification.email;

import com.aijobportal.notification.config.MailProperties;
import com.aijobportal.notification.email.dto.EmailMessage;
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
import com.aijobportal.notification.event.PasswordResetRequestedEvent;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailTemplateService {

    private final EmailLayout layout;
    private final MailProperties mailProperties;

    public EmailTemplateService(EmailLayout layout, MailProperties mailProperties) {
        this.layout = layout;
        this.mailProperties = mailProperties;
    }

    public EmailMessage buildEmployerRegistered(EmployerRegisteredEvent event) {
        Map<String, String> values = baseUser(event.userName(), event.companyName());
        values.put("loginUrl", url("/login"));
        return build(EmailTemplateType.EMPLOYER_REGISTERED, event.email(), values);
    }

    public EmailMessage buildEmployerApproved(EmployerApprovedEvent event) {
        Map<String, String> values = baseUser(event.userName(), event.companyName());
        values.put("dashboardUrl", url("/employer/dashboard"));
        return build(EmailTemplateType.EMPLOYER_APPROVED, event.email(), values);
    }

    public EmailMessage buildEmployerRejected(EmployerRejectedEvent event) {
        Map<String, String> values = baseUser(event.userName(), event.companyName());
        values.put("reason", EmailLayout.reasonOrDefault(event.reason()));
        values.put("profileUrl", url("/employer/profile"));
        return build(EmailTemplateType.EMPLOYER_REJECTED, event.email(), values);
    }

    public EmailMessage buildEmployerHeld(EmployerAccountHeldEvent event) {
        Map<String, String> values = baseUser(event.userName(), event.companyName());
        values.put("reason", EmailLayout.reasonOrDefault(event.reason()));
        return build(EmailTemplateType.EMPLOYER_HELD, event.email(), values);
    }

    public EmailMessage buildEmployerRestored(EmployerAccountRestoredEvent event) {
        Map<String, String> values = baseUser(event.userName(), event.companyName());
        values.put("dashboardUrl", url("/employer/dashboard"));
        return build(EmailTemplateType.EMPLOYER_RESTORED, event.email(), values);
    }

    public EmailMessage buildCandidateHeld(CandidateAccountHeldEvent event) {
        Map<String, String> values = new HashMap<>();
        values.put("userName", event.userName());
        values.put("reason", EmailLayout.reasonOrDefault(event.reason()));
        return build(EmailTemplateType.CANDIDATE_HELD, event.email(), values);
    }

    public EmailMessage buildCandidateRestored(CandidateAccountRestoredEvent event) {
        Map<String, String> values = new HashMap<>();
        values.put("userName", event.userName());
        values.put("dashboardUrl", url("/candidate/dashboard"));
        return build(EmailTemplateType.CANDIDATE_RESTORED, event.email(), values);
    }

    public EmailMessage buildApplicationSubmitted(ApplicationSubmittedEvent event) {
        Map<String, String> values = applicationValues(event.candidateName(), event.jobTitle(), event.companyName());
        values.put("applicationsUrl", url("/candidate/applications"));
        return build(EmailTemplateType.APPLICATION_SUBMITTED, event.candidateEmail(), values);
    }

    public EmailMessage buildCandidateShortlisted(CandidateShortlistedEvent event) {
        Map<String, String> values = applicationValues(event.candidateName(), event.jobTitle(), event.companyName());
        values.put("applicationsUrl", url("/candidate/applications"));
        return build(EmailTemplateType.CANDIDATE_SHORTLISTED, event.candidateEmail(), values);
    }

    public EmailMessage buildCandidateRejected(CandidateRejectedEvent event) {
        Map<String, String> values = applicationValues(event.candidateName(), event.jobTitle(), event.companyName());
        values.put("jobsUrl", url("/jobs"));
        return build(EmailTemplateType.CANDIDATE_REJECTED, event.candidateEmail(), values);
    }

    public EmailMessage buildPasswordReset(PasswordResetRequestedEvent event) {
        Map<String, String> values = new HashMap<>();
        values.put("userName", event.userName());
        values.put("resetLink", event.resetLink());
        values.put("expirationMinutes", String.valueOf(mailProperties.getPasswordReset().getExpirationMinutes()));
        return build(EmailTemplateType.PASSWORD_RESET, event.email(), values);
    }

    private EmailMessage build(EmailTemplateType type, String to, Map<String, String> values) {
        String bodyTemplate = layout.loadBodyTemplate(type);
        String body = layout.wrap(layout.render(bodyTemplate, values));
        String subject = layout.renderSubject(type.subjectTemplate(), values);
        return new EmailMessage(to, subject, body);
    }

    private Map<String, String> baseUser(String userName, String companyName) {
        Map<String, String> values = new HashMap<>();
        values.put("userName", userName);
        values.put("companyName", companyName);
        return values;
    }

    private Map<String, String> applicationValues(String userName, String jobTitle, String companyName) {
        Map<String, String> values = new HashMap<>();
        values.put("userName", userName);
        values.put("jobTitle", jobTitle);
        values.put("companyName", companyName);
        return values;
    }

    private String url(String path) {
        String base = mailProperties.getFrontendUrl();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + path;
    }
}
