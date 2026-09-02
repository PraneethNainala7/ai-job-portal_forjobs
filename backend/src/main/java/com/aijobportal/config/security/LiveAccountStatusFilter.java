package com.aijobportal.config.security;

import com.aijobportal.auth.entity.User;
import com.aijobportal.auth.repository.UserRepository;
import com.aijobportal.common.domain.AccountStatus;
import com.aijobportal.common.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class LiveAccountStatusFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public LiveAccountStatusFilter(UserRepository userRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (isPublicAuthPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            filterChain.doFilter(request, response);
            return;
        }

        User user = userRepository.findById(principal.id()).orElse(null);
        if (user == null) {
            SecurityContextHolder.clearContext();
            write(response, HttpStatus.UNAUTHORIZED, "Sign-in required.");
            return;
        }

        if (user.getAccountStatus() == AccountStatus.INACTIVE && !allowsInactiveSession(request)) {
            SecurityContextHolder.clearContext();
            write(response, HttpStatus.FORBIDDEN, "This account is inactive.");
            return;
        }

        if (user.getAccountStatus() != principal.accountStatus()) {
            AuthPrincipal fresh = new AuthPrincipal(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getRole(),
                    user.getAccountStatus()
            );
            var updated = new UsernamePasswordAuthenticationToken(
                    fresh,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + fresh.role().name()))
            );
            SecurityContextHolder.getContext().setAuthentication(updated);
        }

        filterChain.doFilter(request, response);
    }

    private boolean allowsInactiveSession(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod()) && !"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI();
        return "/api/auth/session".equals(path) || "/api/auth/logout".equals(path);
    }

    private boolean isPublicAuthPath(String path) {
        return "/api/auth/register".equals(path)
                || "/api/auth/login".equals(path)
                || "/api/auth/google".equals(path)
                || "/api/auth/google-config".equals(path)
                || "/api/auth/forgot-password".equals(path)
                || "/api/auth/reset-password".equals(path);
    }

    private void write(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), new ErrorResponse(message));
    }
}
