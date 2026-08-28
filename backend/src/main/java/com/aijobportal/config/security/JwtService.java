package com.aijobportal.config.security;

import com.aijobportal.common.domain.AccountStatus;
import com.aijobportal.common.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs
    ) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            bytes = java.util.Arrays.copyOf(bytes, 32);
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expirationMs = expirationMs;
    }

    public String createToken(AuthPrincipal principal, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
                .subject(principal.id())
                .claim("email", principal.email())
                .claim("name", principal.name())
                .claim("role", principal.role().name())
                .claim("accountStatus", principal.accountStatus().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)));
        extraClaims.forEach(builder::claim);
        return builder.signWith(key).compact();
    }

    public AuthPrincipal parse(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return new AuthPrincipal(
                claims.getSubject(),
                claims.get("email", String.class),
                claims.get("name", String.class),
                Role.valueOf(claims.get("role", String.class)),
                AccountStatus.valueOf(claims.get("accountStatus", String.class))
        );
    }

    public long expirationSeconds() {
        return expirationMs / 1000;
    }
}
