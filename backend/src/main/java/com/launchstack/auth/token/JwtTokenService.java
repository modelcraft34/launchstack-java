package com.launchstack.auth.token;

import com.launchstack.role.Role;
import com.launchstack.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = createSigningKey(jwtProperties.secret());
    }

    public String generateAccessToken(User user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(jwtProperties.accessTokenExpiration());
        List<String> roles = user.getRoles().stream().map(Role::getName).sorted().toList();

        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claim("uid", user.getId())
                .claim("roles", roles)
                .signWith(signingKey)
                .compact();
    }

    public Claims parse(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getAccessTokenExpirationSeconds() {
        return jwtProperties.accessTokenExpiration();
    }

    public long getRefreshTokenExpirationSeconds() {
        return jwtProperties.refreshTokenExpiration();
    }

    private SecretKey createSigningKey(String secret) {
        try {
            return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        } catch (WeakKeyException exception) {
            throw new IllegalStateException(
                    "JWT secret must be at least 32 characters for HS256 signing.", exception);
        }
    }
}
