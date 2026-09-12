package com.aiinterview.backend.security.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final Logger log =
            LoggerFactory.getLogger(OAuth2AuthenticationFailureHandler.class);

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Autowired
    private HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException, ServletException {

        // Clean up authorization cookies
        if (cookieAuthorizationRequestRepository != null) {
            cookieAuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
        }

        log.warn("OAuth2 authentication failure: {}", exception != null ? exception.getMessage() : "Unknown");

        String rawError = exception != null && exception.getMessage() != null ? exception.getMessage() : "";
        String errorCode = "oauth_failed";
        String userFriendlyMessage = "Unable to complete sign-in with your provider. Please try again.";

        if (rawError.contains("authorization_request_not_found")) {
            errorCode = "session_expired";
            userFriendlyMessage = "Your sign-in session expired or was interrupted. Please try again.";
        } else if (rawError.contains("access_denied") || rawError.contains("user_cancelled")) {
            errorCode = "access_denied";
            userFriendlyMessage = "Sign-in was cancelled with the provider.";
        } else if (rawError.contains("invalid_token") || rawError.contains("invalid_state")) {
            errorCode = "security_verification_failed";
            userFriendlyMessage = "Security verification failed. Please try again.";
        }

        String redirectUrl = frontendUrl
                + "/login?oauthError="
                + URLEncoder.encode(errorCode, StandardCharsets.UTF_8)
                + "&errorMsg="
                + URLEncoder.encode(userFriendlyMessage, StandardCharsets.UTF_8);

        getRedirectStrategy().sendRedirect(
                request,
                response,
                redirectUrl
        );
    }
}