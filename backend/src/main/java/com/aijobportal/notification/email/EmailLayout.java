package com.aijobportal.notification.email;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EmailLayout {

    private static final String TEMPLATE_BASE = "templates/email/";
    private static final String DEFAULT_REASON = "No additional details were provided.";

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public String wrap(String body) {
        return load("layout/header.txt") + "\n\n" + body + "\n\n" + load("layout/footer.txt");
    }

    public String render(String template, Map<String, String> values) {
        String output = template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String replacement = entry.getValue() == null ? "" : entry.getValue();
            output = output.replace("{{" + entry.getKey() + "}}", replacement);
        }
        return output.replaceAll("\\{\\{[^}]+}}", "");
    }

    public String renderSubject(String subjectTemplate, Map<String, String> values) {
        return render(subjectTemplate, values);
    }

    public String loadBodyTemplate(EmailTemplateType type) {
        return load(type.fileName() + ".txt");
    }

    public static String reasonOrDefault(String reason) {
        return reason == null || reason.isBlank() ? DEFAULT_REASON : reason.trim();
    }

    private String load(String path) {
        return cache.computeIfAbsent(path, this::readClasspath);
    }

    private String readClasspath(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(TEMPLATE_BASE + path);
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Missing email template: " + path, ex);
        }
    }
}
