package com.aijobportal.config.security;

import com.aijobportal.auth.entity.User;
import com.aijobportal.auth.repository.UserRepository;
import com.aijobportal.common.domain.AccountStatus;
import com.aijobportal.common.domain.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LiveAccountStatusFilterTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private FilterChain filterChain;

    private LiveAccountStatusFilter filter;

    @BeforeEach
    void setUp() {
        filter = new LiveAccountStatusFilter(userRepository, new ObjectMapper());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void replacesStaleJwtStatusWithDatabaseStatus() throws Exception {
        AuthPrincipal stale = new AuthPrincipal("user-1", "a@test.com", "Ada", Role.EMPLOYER, AccountStatus.ACTIVE);
        setAuthentication(stale);

        User user = activeUser(AccountStatus.ON_HOLD);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), filterChain);

        AuthPrincipal fresh = principal();
        assertEquals(AccountStatus.ON_HOLD, fresh.accountStatus());
        verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void allowsInactiveAccountToRefreshSession() throws Exception {
        AuthPrincipal stale = new AuthPrincipal("user-1", "a@test.com", "Ada", Role.EMPLOYER, AccountStatus.ACTIVE);
        setAuthentication(stale);

        User user = activeUser(AccountStatus.INACTIVE);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/session");
        filter.doFilter(request, new MockHttpServletResponse(), filterChain);

        AuthPrincipal fresh = principal();
        assertEquals(AccountStatus.INACTIVE, fresh.accountStatus());
        verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void allowsRegisterWhenInactiveSessionCookiePresent() throws Exception {
        AuthPrincipal stale = new AuthPrincipal("user-1", "a@test.com", "Ada", Role.CANDIDATE, AccountStatus.ACTIVE);
        setAuthentication(stale);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/register");
        filter.doFilter(request, new MockHttpServletResponse(), filterChain);

        verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsInactiveAccounts() throws Exception {
        AuthPrincipal stale = new AuthPrincipal("user-1", "a@test.com", "Ada", Role.EMPLOYER, AccountStatus.ACTIVE);
        setAuthentication(stale);

        User user = activeUser(AccountStatus.INACTIVE);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest(), response, filterChain);

        assertEquals(403, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private void setAuthentication(AuthPrincipal principal) {
        var authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + principal.role().name()))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private AuthPrincipal principal() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertInstanceOf(AuthPrincipal.class, authentication.getPrincipal());
        return (AuthPrincipal) authentication.getPrincipal();
    }

    private User activeUser(AccountStatus status) {
        User user = new User();
        user.setId("user-1");
        user.setEmail("a@test.com");
        user.setName("Ada");
        user.setRole(Role.EMPLOYER);
        user.setAccountStatus(status);
        return user;
    }
}
