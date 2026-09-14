package com.aiinterview.backend.security;

import com.aiinterview.backend.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService customUserDetailsService;

    public JwtFilter(
            CustomUserDetailsService customUserDetailsService
    ) {
        this.customUserDetailsService =
                customUserDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {

        String path =
                request.getServletPath();

        return path.equals("/")
                || path.equals("/test")
                || path.equals("/login")
                || path.equals("/register")
                || path.startsWith("/api/auth/")
                || path.equals("/api/feedback/approve")
                || path.equals("/api/feedback/reject")
                || path.equals("/api/feedback/public");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    )
            throws ServletException, IOException {

        String authHeader =
                request.getHeader(
                        "Authorization"
                );

        if (
                authHeader == null ||
                !authHeader.toLowerCase().startsWith("bearer ")
        ) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String rawToken = authHeader.substring(7).trim();
        while (rawToken.toLowerCase().startsWith("bearer ")) {
            rawToken = rawToken.substring(7).trim();
        }
        String token = rawToken.replace("\"", "").trim();

        if (token.isBlank()) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        String email = null;
        try {
            io.jsonwebtoken.Claims claims = JwtUtil.parseClaims(token);
            email = claims.getSubject();
        } catch (io.jsonwebtoken.ExpiredJwtException eje) {
            int len = token.length();
            String prefix = len >= 10 ? token.substring(0, 10) : token;
            String suffix = len >= 10 ? token.substring(len - 10) : token;
            org.slf4j.LoggerFactory.getLogger(JwtFilter.class)
                    .warn("JwtFilter expired token for URI [{}] (tokenLen={}, prefix='{}...', suffix='...{}', expiredAt={})",
                            request.getRequestURI(), len, prefix, suffix, eje.getClaims().getExpiration());
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        } catch (io.jsonwebtoken.security.SignatureException | io.jsonwebtoken.MalformedJwtException mje) {
            int len = token.length();
            String prefix = len >= 10 ? token.substring(0, 10) : token;
            String suffix = len >= 10 ? token.substring(len - 10) : token;
            org.slf4j.LoggerFactory.getLogger(JwtFilter.class)
                    .warn("JwtFilter invalid signature/malformed token for URI [{}] (tokenLen={}, prefix='{}...', suffix='...{}'): {}",
                            request.getRequestURI(), len, prefix, suffix, mje.getMessage());
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        } catch (Exception ex) {
            int len = token.length();
            String prefix = len >= 10 ? token.substring(0, 10) : token;
            String suffix = len >= 10 ? token.substring(len - 10) : token;
            org.slf4j.LoggerFactory.getLogger(JwtFilter.class)
                    .warn("JwtFilter token parse failed for URI [{}] (tokenLen={}, prefix='{}...', suffix='...{}'): {}",
                            request.getRequestURI(), len, prefix, suffix, ex.getMessage());
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        if (email == null || email.isBlank()) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        try {

            UserDetails userDetails =
                    customUserDetailsService
                            .loadUserByUsername(
                                    email
                            );

            if (
                    userDetails == null ||
                    !userDetails.isEnabled()
            ) {

                SecurityContextHolder
                        .clearContext();

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }

            UsernamePasswordAuthenticationToken
                    authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(
                                    request
                            )
            );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            authentication
                    );

            request.setAttribute("email", email);

        } catch (Exception exception) {
            org.slf4j.LoggerFactory.getLogger(JwtFilter.class)
                    .error("JwtFilter authentication error for URI [{}]: {}", request.getRequestURI(), exception.getMessage(), exception);

            SecurityContextHolder
                    .clearContext();
        }

        filterChain.doFilter(
                request,
                response
        );
    }
}