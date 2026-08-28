package com.aijobportal.config.security;

import com.aijobportal.common.domain.AccountStatus;
import com.aijobportal.common.domain.Role;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtServiceTest {

    @Test
    void roundTripClaims() {
        JwtService jwtService = new JwtService("unit-test-secret-key-value-that-is-long-enough", 60_000);
        AuthPrincipal principal = new AuthPrincipal("user-1", "a@demo.com", "Ada", Role.CANDIDATE, AccountStatus.ACTIVE);
        String token = jwtService.createToken(principal, Map.of());
        AuthPrincipal parsed = jwtService.parse(token);
        assertEquals("user-1", parsed.id());
        assertEquals(Role.CANDIDATE, parsed.role());
        assertEquals(AccountStatus.ACTIVE, parsed.accountStatus());
    }
}
