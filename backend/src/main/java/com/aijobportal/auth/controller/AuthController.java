package com.aijobportal.auth.controller;

import com.aijobportal.auth.dto.AuthResponse;
import com.aijobportal.auth.dto.LoginRequest;
import com.aijobportal.auth.dto.OkResponse;
import com.aijobportal.auth.dto.RegisterRequest;
import com.aijobportal.auth.dto.SessionResponse;
import com.aijobportal.auth.entity.User;
import com.aijobportal.auth.service.AuthService;
import com.aijobportal.config.security.AuthPrincipal;
import com.aijobportal.config.security.AuthCookieService;
import com.aijobportal.config.security.SecurityUtils;
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
    private final AuthCookieService authCookieService;
    private final CompanyRepository companyRepository;

    public AuthController(
            AuthService authService,
            AuthCookieService authCookieService,
            CompanyRepository companyRepository
    ) {
        this.authService = authService;
        this.authCookieService = authCookieService;
        this.companyRepository = companyRepository;
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

    private void writeCookie(HttpServletResponse response, User user) {
        authCookieService.setSession(response, user, companyRepository.findByEmployerId(user.getId()).orElse(null));
    }
}
