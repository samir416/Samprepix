package com.aiinterview.backend.security;

import com.aiinterview.backend.security.oauth.CustomOAuth2UserService;
import com.aiinterview.backend.security.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.aiinterview.backend.security.oauth.OAuth2AuthenticationFailureHandler;
import com.aiinterview.backend.security.oauth.OAuth2AuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import java.util.List;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

        @org.springframework.beans.factory.annotation.Value("${app.frontend.url:http://localhost:5173}")
        private String frontendUrl;

        private final JwtFilter jwtFilter;
        private final CustomOAuth2UserService customOAuth2UserService;
        private final com.aiinterview.backend.security.oauth.CustomOidcUserService customOidcUserService;
        private final OAuth2AuthenticationSuccessHandler successHandler;
        private final OAuth2AuthenticationFailureHandler failureHandler;
        private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

        public SecurityConfig(
                        JwtFilter jwtFilter,
                        CustomOAuth2UserService customOAuth2UserService,
                        com.aiinterview.backend.security.oauth.CustomOidcUserService customOidcUserService,
                        OAuth2AuthenticationSuccessHandler successHandler,
                        OAuth2AuthenticationFailureHandler failureHandler,
                        HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository) {

                this.jwtFilter = jwtFilter;
                this.customOAuth2UserService = customOAuth2UserService;
                this.customOidcUserService = customOidcUserService;
                this.successHandler = successHandler;
                this.failureHandler = failureHandler;
                this.cookieAuthorizationRequestRepository = cookieAuthorizationRequestRepository;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http) throws Exception {

                http
                                .csrf(csrf -> csrf.disable())

                                .cors(cors -> cors.configurationSource(request -> {
                                        CorsConfiguration config = new CorsConfiguration();

                                        java.util.Set<String> allowedOrigins = new java.util.LinkedHashSet<>();
                                        allowedOrigins.add("http://localhost:5173");
                                        allowedOrigins.add("http://127.0.0.1:5173");
                                        allowedOrigins.add("http://[::1]:5173");
                                        if (frontendUrl != null && !frontendUrl.isBlank()) {
                                                allowedOrigins.add(frontendUrl.trim());
                                        }

                                        config.setAllowedOrigins(new java.util.ArrayList<>(allowedOrigins));

                                        config.setAllowedMethods(List.of(
                                                        "GET",
                                                        "POST",
                                                        "PUT",
                                                        "DELETE",
                                                        "PATCH",
                                                        "OPTIONS"));

                                        config.setAllowedHeaders(List.of("*"));
                                        config.setAllowCredentials(true);

                                        return config;
                                }))

                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))

                                .oauth2Login(oauth -> oauth

                                                .authorizationEndpoint(
                                                                endpoint -> endpoint.baseUri("/oauth2/authorize")
                                                                                .authorizationRequestRepository(cookieAuthorizationRequestRepository))

                                                .redirectionEndpoint(
                                                                endpoint -> endpoint.baseUri("/login/oauth2/code/*"))

                                                .userInfoEndpoint(user -> user
                                                                .userService(customOAuth2UserService)
                                                                .oidcUserService(customOidcUserService))

                                                .successHandler(successHandler)

                                                .failureHandler(failureHandler))

                                                .exceptionHandling(exception -> exception
                                                                .defaultAuthenticationEntryPointFor(
                                                                                (request, response, authException) -> {
                                                                                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                                                                        response.setContentType("application/json;charset=UTF-8");
                                                                                        response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication required. Please provide a valid Bearer token.\"}");
                                                                                },
                                                                                request -> request.getRequestURI().startsWith("/api/"))
                                                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                                                        response.setStatus(HttpStatus.FORBIDDEN.value());
                                                                        response.setContentType("application/json;charset=UTF-8");
                                                                        response.getWriter().write("{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access Denied: ADMIN role required.\"}");
                                                                }))

                                .authorizeHttpRequests(auth -> auth

                                                .requestMatchers(
                                                                "/",
                                                                "/test",
                                                                "/error",
                                                                "/login",
                                                                "/register",
                                                                "/verify-otp",
                                                                "/resend-otp",
                                                                "/forgot-password",
                                                                "/reset-password",
                                                                "/api/auth/**",
                                                                "/oauth2/**",
                                                                "/login/oauth2/**",
                                                                "/uploads/**",
                                                                "/api/feedback/public",
                                                                "/api/feedback/approve",
                                                                "/api/feedback/reject",
                                                                "/api/aptitude/**",
                                                                "/api/plans/**",
                                                                "/api/payment/webhook")
                                                .permitAll()
                                                // Admin endpoints require ADMIN role (enforced at both URL and method level)
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/api/subscription/admin/**").hasRole("ADMIN")
                                                .anyRequest()
                                                .authenticated())

                                .addFilterBefore(
                                                jwtFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

}
