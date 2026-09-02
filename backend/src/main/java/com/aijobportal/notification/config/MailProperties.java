package com.aijobportal.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class MailProperties {

    private Mail mail = new Mail();
    private String frontendUrl = "http://localhost:3000";
    private PasswordReset passwordReset = new PasswordReset();

    public Mail getMail() {
        return mail;
    }

    public void setMail(Mail mail) {
        this.mail = mail;
    }

    public String getFrontendUrl() {
        return frontendUrl;
    }

    public void setFrontendUrl(String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    public PasswordReset getPasswordReset() {
        return passwordReset;
    }

    public void setPasswordReset(PasswordReset passwordReset) {
        this.passwordReset = passwordReset;
    }

    public static class Mail {
        private boolean enabled;
        private String fromName = "AI Job Portal";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getFromName() {
            return fromName;
        }

        public void setFromName(String fromName) {
            this.fromName = fromName;
        }
    }

    public static class PasswordReset {
        private int expirationMinutes = 60;

        public int getExpirationMinutes() {
            return expirationMinutes;
        }

        public void setExpirationMinutes(int expirationMinutes) {
            this.expirationMinutes = expirationMinutes;
        }
    }
}
