package com.aijobportal.config.security;

import com.aijobportal.auth.entity.User;
import com.aijobportal.employer.entity.Company;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthCookieService {

    private final JwtService jwtService;
    private final String cookieName;
    private final boolean secure;

    public AuthCookieService(
            JwtService jwtService,
            @Value("${app.cookie-name}") String cookieName,
            @Value("${app.cookie-secure}") boolean secure
    ) {
        this.jwtService = jwtService;
        this.cookieName = cookieName;
        this.secure = secure;
    }

    public void setSession(HttpServletResponse response, User user, Company company) {
        AuthPrincipal principal = new AuthPrincipal(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getAccountStatus()
        );
        Map<String, Object> extra = new HashMap<>();
        if (company != null) {
            extra.put("companyName", company.getCompanyName());
            extra.put("companyInformation", company.getDescription());
            extra.put("companyLocation", company.getLocation());
            extra.put("cin", company.getCin());
            extra.put("companyWebsite", company.getWebsite());
        }
        if (user.getStatusReason() != null && user.getAccountStatus().name().equals("REJECTED")) {
            extra.put("rejectionReason", user.getStatusReason());
        }
        String token = jwtService.createToken(principal, extra);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(token, jwtService.expirationSeconds()).toString());
    }

    public void clearSession(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie("", 0).toString());
    }

    private ResponseCookie cookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from(cookieName, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .build();
    }
}
