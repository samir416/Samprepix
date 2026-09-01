package com.aiinterview.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public final class JwtUtil {

    private static final long EXPIRATION_TIME =
            1000L * 60 * 60 * 24;

    private static final SecretKey SECRET_KEY =
            createSecretKey();

    private JwtUtil() {
    }

    private static SecretKey createSecretKey() {

        String secret =
                System.getProperty("app.jwt.secret");

        if (
                secret == null ||
                secret.isBlank()
        ) {
            secret =
                    System.getenv("APP_JWT_SECRET");
        }

        if (
                secret == null ||
                secret.isBlank()
        ) {
            throw new IllegalStateException(
                    "JWT secret is not configured. Set app.jwt.secret or APP_JWT_SECRET."
            );
        }

        if (
                secret.getBytes(
                        StandardCharsets.UTF_8
                ).length < 32
        ) {
            throw new IllegalStateException(
                    "JWT secret must contain at least 32 bytes."
            );
        }

        return Keys.hmacShaKeyFor(
                secret.getBytes(
                        StandardCharsets.UTF_8
                )
        );
    }

    public static String generateToken(
            String email
    ) {

        if (
                email == null ||
                email.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Email is required to generate JWT."
            );
        }

        return Jwts.builder()
                .subject(
                        email.trim()
                )
                .issuedAt(
                        new Date()
                )
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + EXPIRATION_TIME
                        )
                )
                .signWith(
                        SECRET_KEY
                )
                .compact();
    }

    public static String extractEmail(
            String token
    ) {

        if (
                token == null ||
                token.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "JWT token is required."
            );
        }

        Claims claims =
                Jwts.parser()
                        .verifyWith(
                                SECRET_KEY
                        )
                        .build()
                        .parseSignedClaims(
                                token
                        )
                        .getPayload();

        return claims.getSubject();
    }

    public static boolean validateToken(
            String token
    ) {

        if (
                token == null ||
                token.isBlank()
        ) {
            return false;
        }

        try {

            Jwts.parser()
                    .verifyWith(
                            SECRET_KEY
                    )
                    .build()
                    .parseSignedClaims(
                            token
                    );

            return true;

        } catch (Exception exception) {

            return false;
        }
    }
}