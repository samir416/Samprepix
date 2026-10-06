package com.aiinterview.backend.security;

import com.aiinterview.backend.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(JwtFilter.class);

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
        String path = request.getServletPath();

        return path.equals("/")
                || path.equals("/test")
                || path.equals("/login")
                || path.equals("/register")
                || path.equals("/verify-otp")
                || path.equals("/resend-otp")
                || path.equals("/forgot-password")
                || path.equals("/reset-password")
                || path.startsWith("/api/auth/")
                || path.startsWith("/oauth2/")
                || path.startsWith("/login/oauth2/")
                || path.equals("/api/feedback/approve")
                || path.equals("/api/feedback/reject")
                || path.equals("/api/feedback/public")
                || path.equals("/api/payment/webhook");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        boolean authHeaderPresent = (authHeader != null && !authHeader.isBlank());
        log.debug("JWT_DEBUG: authorization header present = {}", authHeaderPresent);

        if (!authHeaderPresent) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authHeader.regionMatches(
                true,
                0,
                "Bearer ",
                0,
                7
        )) {
            log.debug("JWT_DEBUG: Authorization header does not start with Bearer");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();
        boolean tokenPresent = (token != null && !token.isBlank());
        log.debug("JWT_DEBUG: token present = {}", tokenPresent);

        if (token.isBlank()
                || token.length() > 4096
                || token.indexOf('"') >= 0
                || token.indexOf('\r') >= 0
                || token.indexOf('\n') >= 0) {

            log.debug("JWT_DEBUG: token format rejected (length={}, hasQuotes={})", token.length(), token.indexOf('"') >= 0);
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String email;

        try {
            Claims claims = JwtUtil.parseClaims(token);
            email = claims.getSubject();
            log.debug("JWT_DEBUG: JWT parsed = true, JWT subject/email = {}", email);

        } catch (ExpiredJwtException exception) {
            SecurityContextHolder.clearContext();
            log.debug("JWT_DEBUG: JWT parsed = false (expired)");
            filterChain.doFilter(request, response);
            return;

        } catch (JwtException | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
            log.debug("JWT_DEBUG: JWT parsed = false (invalid: {})", exception.getMessage());
            filterChain.doFilter(request, response);
            return;

        } catch (Exception exception) {
            SecurityContextHolder.clearContext();
            log.debug("JWT_DEBUG: JWT parsed = false (error: {})", exception.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (email == null
                || email.isBlank()
                || email.length() > 254
                || email.indexOf('\r') >= 0
                || email.indexOf('\n') >= 0) {

            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        try {

            UserDetails userDetails =
                    customUserDetailsService
                            .loadUserByUsername(email.trim());

            boolean userFound = (userDetails != null);
            log.debug("JWT_DEBUG: user found = {}", userFound);

            if (userDetails == null
                    || !userDetails.isEnabled()
                    || !userDetails.isAccountNonLocked()
                    || !userDetails.isAccountNonExpired()) {

                log.debug("JWT_DEBUG: user isEnabled={}, isAccountNonLocked={}, isAccountNonExpired={}",
                        (userDetails != null && userDetails.isEnabled()),
                        (userDetails != null && userDetails.isAccountNonLocked()),
                        (userDetails != null && userDetails.isAccountNonExpired()));

                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

            request.setAttribute("email", email.trim());
            log.debug("JWT_DEBUG: authentication created = true");

        } catch (Exception exception) {

            SecurityContextHolder.clearContext();
            log.debug("JWT_DEBUG: user authentication failed: {}", exception.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}