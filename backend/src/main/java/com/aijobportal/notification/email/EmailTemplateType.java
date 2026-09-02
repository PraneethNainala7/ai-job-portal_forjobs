package com.aijobportal.notification.email;

public enum EmailTemplateType {
    EMPLOYER_REGISTERED("employer-registered", "Registration received — {{companyName}}"),
    EMPLOYER_APPROVED("employer-approved", "Your employer account is approved"),
    EMPLOYER_REJECTED("employer-rejected", "Employer registration update"),
    EMPLOYER_HELD("employer-held", "Your employer account is on hold"),
    EMPLOYER_RESTORED("employer-restored", "Your employer account access has been restored"),
    CANDIDATE_HELD("candidate-held", "Your candidate account is on hold"),
    CANDIDATE_RESTORED("candidate-restored", "Your candidate account access has been restored"),
    APPLICATION_SUBMITTED("application-submitted", "Application received — {{jobTitle}} at {{companyName}}"),
    CANDIDATE_SHORTLISTED("candidate-shortlisted", "Update on your application — {{jobTitle}}"),
    CANDIDATE_REJECTED("candidate-rejected", "Update on your application — {{jobTitle}}"),
    PASSWORD_RESET("password-reset", "Reset your AI Job Portal password");

    private final String fileName;
    private final String subjectTemplate;

    EmailTemplateType(String fileName, String subjectTemplate) {
        this.fileName = fileName;
        this.subjectTemplate = subjectTemplate;
    }

    public String fileName() {
        return fileName;
    }

    public String subjectTemplate() {
        return subjectTemplate;
    }
}
