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
                        CustomUserDetailsService customUserDetailsService) {

                this.customUserDetailsService = customUserDetailsService;
        }

        @Override
        protected void doFilterInternal(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        FilterChain filterChain)
                        throws ServletException, IOException {

                System.out.println("JWT FILTER HIT");

                String authHeader = request.getHeader("Authorization");

                System.out.println("HEADER = " + authHeader);

                if (authHeader == null
                                || !authHeader.startsWith("Bearer ")) {

                        filterChain.doFilter(request, response);
                        return;
                }

                String token = authHeader.substring(7);

                System.out.println("TOKEN = " + token);

                if (!JwtUtil.validateToken(token)) {

                        System.out.println("INVALID TOKEN");

                        filterChain.doFilter(request, response);
                        return;
                }

                String email = JwtUtil.extractEmail(token);
                request.setAttribute(
                                "email",
                                email);

                System.out.println("EMAIL = " + email);

                UserDetails userDetails = customUserDetailsService
                                .loadUserByUsername(email);

                System.out.println("USER FOUND");

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());

                authToken.setDetails(
                                new WebAuthenticationDetailsSource()
                                                .buildDetails(request));

                SecurityContextHolder
                                .getContext()
                                .setAuthentication(authToken);

                System.out.println("AUTHENTICATION SET");

                filterChain.doFilter(request, response);
        }
}