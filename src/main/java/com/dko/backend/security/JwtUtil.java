package com.dko.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    private static final String DEV_SECRET_PREFIX = "dko-dev-secret";

    private final Key key;

    @org.springframework.beans.factory.annotation.Autowired
    public JwtUtil(@org.springframework.beans.factory.annotation.Value("${jwt.secret}") String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET is not set. Set it via environment variable with at least 32 characters.");
        }
        if (secret.length() < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET must be at least 32 characters long for HS256 signing.");
        }
        if (secret.startsWith(DEV_SECRET_PREFIX)) {
            log.warn("⚠ Using default dev JWT secret. Set JWT_SECRET env variable before deploying to production!");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    private static final long ACCESS_TOKEN_EXPIRY = 15 * 60 * 1000; // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRY = 7 * 24 * 60 * 60 * 1000; // 7 days

    private static final String ISSUER = "developer-knowledge-organizer";

    public String generateAccessToken(String userId, String role) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRY))
                .setIssuer(ISSUER)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRY))
                .claim("type", "refresh")
                .setIssuer(ISSUER)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUserId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public String extractRole(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("role", String.class);
    }

    public void validateToken(String token) {
        Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    public long getAccessTokenExpiryInSeconds() {
        return ACCESS_TOKEN_EXPIRY / 1000;
    }

    public long getRefreshTokenExpiryInSeconds() {
        return REFRESH_TOKEN_EXPIRY / 1000;
    }
}
