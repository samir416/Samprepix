package com.aiinterview.backend.security;

import com.aiinterview.backend.security.oauth.CustomOAuth2UserService;
import com.aiinterview.backend.security.oauth.CustomOidcUserService;
import com.aiinterview.backend.security.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.aiinterview.backend.security.oauth.OAuth2AuthenticationFailureHandler;
import com.aiinterview.backend.security.oauth.OAuth2AuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    private final JwtFilter jwtFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOidcUserService customOidcUserService;
    private final OAuth2AuthenticationSuccessHandler successHandler;
    private final OAuth2AuthenticationFailureHandler failureHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    public SecurityConfig(
            JwtFilter jwtFilter,
            CustomOAuth2UserService customOAuth2UserService,
            CustomOidcUserService customOidcUserService,
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

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .contentTypeOptions(contentType -> {})
                        .referrerPolicy(referrer ->
                                referrer.policy(
                                        ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER
                                )
                        )
                        .httpStrictTransportSecurity(hsts ->
                                hsts.includeSubDomains(true)
                                        .maxAgeInSeconds(31536000)
                        )
                )

                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(endpoint ->
                                endpoint
                                        .baseUri("/oauth2/authorize")
                                        .authorizationRequestRepository(
                                                cookieAuthorizationRequestRepository
                                        )
                        )
                        .redirectionEndpoint(endpoint ->
                                endpoint.baseUri("/login/oauth2/code/*")
                        )
                        .userInfoEndpoint(user -> user
                                .userService(customOAuth2UserService)
                                .oidcUserService(customOidcUserService)
                        )
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                )

                .exceptionHandling(exception -> exception
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(
                                        HttpStatus.UNAUTHORIZED
                                ),
                                request ->
                                        request.getRequestURI()
                                                .startsWith("/api/")
                        )
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType(
                                    "application/json;charset=UTF-8"
                            );
                            response.getWriter().write(
                                    "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access denied.\"}"
                            );
                        })
                )

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
                                "/api/payment/webhook",
                                "/api/support/**",
                                "/api/ai-roadmap/**",
                                "/api/github-analyzer/**"
                        )
                        .permitAll()

                        .requestMatchers("/api/plans/**")
                        .authenticated()

                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/subscription/admin/**")
                        .hasRole("ADMIN")

                        .anyRequest()
                        .authenticated()
                )

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        Set<String> allowedOrigins = new LinkedHashSet<>();

        allowedOrigins.add("http://localhost:5173");
        allowedOrigins.add("http://127.0.0.1:5173");
        allowedOrigins.add("http://[::1]:5173");

        if (frontendUrl != null && !frontendUrl.isBlank()) {
            String normalizedFrontendUrl = frontendUrl.trim();

            if (normalizedFrontendUrl.startsWith("http://")
                    || normalizedFrontendUrl.startsWith("https://")) {
                allowedOrigins.add(normalizedFrontendUrl);
            }
        }

        config.setAllowedOrigins(
                new ArrayList<>(allowedOrigins)
        );

        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "PATCH",
                "OPTIONS"
        ));

        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "X-Cashfree-Signature",
                "x-webhook-signature",
                "x-webhook-timestamp"
        ));

        config.setExposedHeaders(List.of(
                "Authorization",
                "Content-Disposition"
        ));

        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                config
        );

        return source;
    }
}