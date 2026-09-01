package com.aiinterview.backend.security.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2AuthenticationFailureHandler
        extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException, ServletException {

        String errorMessage =
                exception.getMessage();

        if (errorMessage == null ||
                errorMessage.isBlank()) {

            errorMessage =
                    "OAuth authentication failed.";
        }

        String error =
                URLEncoder.encode(
                        errorMessage,
                        StandardCharsets.UTF_8
                );

        String redirectUrl =
                frontendUrl
                        + "/login?oauthError="
                        + error;

        getRedirectStrategy().sendRedirect(
                request,
                response,
                redirectUrl
        );
    }
}