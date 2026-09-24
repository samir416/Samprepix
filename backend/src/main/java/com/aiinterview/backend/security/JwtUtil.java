package com.aiinterview.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public final class JwtUtil {

    private static final long EXPIRATION_TIME =
            1000L * 60 * 60 * 24 * 7;

    private static final String DEFAULT_SECRET =
            "your-super-secret-key-at-least-32-characters-long";

    private static volatile SecretKey SECRET_KEY =
            createSecretKey(DEFAULT_SECRET);

    public JwtUtil(
            @Value("${app.jwt.secret:}") String secret
    ) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT secret is not configured. Set app.jwt.secret."
            );
        }

        SECRET_KEY = createSecretKey(secret);
    }

    private static SecretKey createSecretKey(String secret) {

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT secret is not configured."
            );
        }

        byte[] keyBytes =
                secret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT secret must contain at least 32 bytes."
            );
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static String generateToken(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email is required to generate JWT."
            );
        }

        String normalizedEmail = email.trim();

        if (normalizedEmail.length() > 254
                || normalizedEmail.contains("\r")
                || normalizedEmail.contains("\n")) {
            throw new IllegalArgumentException(
                    "Invalid email."
            );
        }

        Date issuedAt = new Date();
        Date expiration = new Date(
                issuedAt.getTime() + EXPIRATION_TIME
        );

        return Jwts.builder()
                .subject(normalizedEmail)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(SECRET_KEY)
                .compact();
    }

    public static String extractEmail(String token) {

        Claims claims = parseClaims(token);

        String subject = claims.getSubject();

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT subject is missing."
            );
        }

        return subject;
    }

    public static Claims parseClaims(String token) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT token is required."
            );
        }

        if (token.length() > 4096) {
            throw new IllegalArgumentException(
                    "JWT token is invalid."
            );
        }

        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static boolean validateToken(String token) {

        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            parseClaims(token);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }
}