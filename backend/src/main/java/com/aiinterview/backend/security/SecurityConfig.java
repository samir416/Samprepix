package com.aiinterview.backend.security;

import com.aiinterview.backend.security.oauth.CustomOAuth2UserService;
import com.aiinterview.backend.security.oauth.OAuth2AuthenticationFailureHandler;
import com.aiinterview.backend.security.oauth.OAuth2AuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
public class SecurityConfig {

        private final JwtFilter jwtFilter;
        private final CustomOAuth2UserService customOAuth2UserService;
        private final OAuth2AuthenticationSuccessHandler successHandler;
        private final OAuth2AuthenticationFailureHandler failureHandler;

        public SecurityConfig(
                        JwtFilter jwtFilter,
                        CustomOAuth2UserService customOAuth2UserService,
                        OAuth2AuthenticationSuccessHandler successHandler,
                        OAuth2AuthenticationFailureHandler failureHandler) {

                this.jwtFilter = jwtFilter;
                this.customOAuth2UserService = customOAuth2UserService;
                this.successHandler = successHandler;
                this.failureHandler = failureHandler;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http) throws Exception {

                http
                                .csrf(csrf -> csrf.disable())

                                .cors(cors -> cors.configurationSource(request -> {
                                        CorsConfiguration config = new CorsConfiguration();

                                        config.setAllowedOrigins(List.of(
                                                        "http://localhost:5173"));

                                        config.setAllowedMethods(List.of(
                                                        "GET",
                                                        "POST",
                                                        "PUT",
                                                        "DELETE",
                                                        "OPTIONS"));

                                        config.setAllowedHeaders(List.of("*"));
                                        config.setAllowCredentials(true);

                                        return config;
                                }))

                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))

                                .oauth2Login(oauth -> oauth

                                                .authorizationEndpoint(
                                                                endpoint -> endpoint.baseUri("/oauth2/authorize"))

                                                .redirectionEndpoint(
                                                                endpoint -> endpoint.baseUri("/login/oauth2/code/*"))

                                                .userInfoEndpoint(user -> user.userService(customOAuth2UserService))

                                                .successHandler(successHandler)

                                                .failureHandler(failureHandler))

                                .authorizeHttpRequests(auth -> auth

                                                .requestMatchers(
                                                                "/",
                                                                "/test",
                                                                "/login",
                                                                "/register",
                                                                "/verify-otp",
                                                                "/resend-otp",
                                                                "/forgot-password",
                                                                "/reset-password",
                                                                "/api/auth/**",
                                                                "/oauth2/**",
                                                                "/login/oauth2/**",
                                                                "/api/feedback/public",
                                                                "/api/feedback/approve",
                                                                "/api/feedback/reject")
                                                .permitAll()
                                                .anyRequest()
                                                .authenticated())

                                .addFilterBefore(
                                                jwtFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}