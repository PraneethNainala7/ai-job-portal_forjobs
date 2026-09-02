package com.aijobportal.auth.controller;

import com.aijobportal.auth.dto.AuthResponse;
import com.aijobportal.auth.dto.ForgotPasswordRequest;
import com.aijobportal.auth.dto.GoogleAuthResponse;
import com.aijobportal.auth.dto.GoogleConfigResponse;
import com.aijobportal.auth.dto.GoogleLoginRequest;
import com.aijobportal.auth.dto.LoginRequest;
import com.aijobportal.auth.dto.OkResponse;
import com.aijobportal.auth.dto.RegisterRequest;
import com.aijobportal.auth.dto.ResetPasswordRequest;
import com.aijobportal.auth.dto.SessionResponse;
import com.aijobportal.auth.entity.User;
import com.aijobportal.auth.service.AuthService;
import com.aijobportal.auth.service.GoogleAuthService;
import com.aijobportal.auth.service.PasswordResetService;
import com.aijobportal.config.security.AuthPrincipal;
import com.aijobportal.config.security.AuthCookieService;
import com.aijobportal.config.security.SecurityUtils;
import com.aijobportal.config.GoogleProperties;
import com.aijobportal.employer.repository.CompanyRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;
    private final AuthCookieService authCookieService;
    private final CompanyRepository companyRepository;
    private final PasswordResetService passwordResetService;
    private final GoogleProperties googleProperties;

    public AuthController(
            AuthService authService,
            GoogleAuthService googleAuthService,
            AuthCookieService authCookieService,
            CompanyRepository companyRepository,
            PasswordResetService passwordResetService,
            GoogleProperties googleProperties
    ) {
        this.authService = authService;
        this.googleAuthService = googleAuthService;
        this.authCookieService = authCookieService;
        this.companyRepository = companyRepository;
        this.passwordResetService = passwordResetService;
        this.googleProperties = googleProperties;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        User user = authService.login(request);
        writeCookie(response, user);
        return new AuthResponse(authService.toResponse(user));
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        User user = authService.register(request);
        writeCookie(response, user);
        return new AuthResponse(authService.toResponse(user));
    }

    @PostMapping("/google")
    public GoogleAuthResponse google(@Valid @RequestBody GoogleLoginRequest request, HttpServletResponse response) {
        GoogleAuthService.GoogleAuthResult result = googleAuthService.authenticate(request.idToken(), request.role());
        writeCookie(response, result.user());
        return new GoogleAuthResponse(authService.toResponse(result.user()), result.needsEmployerOnboarding());
    }

    @GetMapping("/google-config")
    public GoogleConfigResponse googleConfig() {
        boolean enabled = googleProperties.isConfigured();
        return new GoogleConfigResponse(enabled ? googleProperties.getClientId() : null, enabled);
    }

    @GetMapping("/session")
    public SessionResponse session(HttpServletResponse response) {
        AuthPrincipal principal = SecurityUtils.currentUserOrNull();
        if (principal == null) {
            return new SessionResponse(null);
        }
        User user = authService.getById(principal.id());
        writeCookie(response, user);
        return new SessionResponse(authService.toResponse(user));
    }

    @PostMapping("/logout")
    public OkResponse logout(HttpServletResponse response) {
        authCookieService.clearSession(response);
        return OkResponse.yes();
    }

    @PostMapping("/forgot-password")
    public OkResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.email());
        return OkResponse.yes();
    }

    @PostMapping("/reset-password")
    public OkResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.password());
        return OkResponse.yes();
    }

    private void writeCookie(HttpServletResponse response, User user) {
        authCookieService.setSession(response, user, companyRepository.findByEmployerId(user.getId()).orElse(null));
    }
}
