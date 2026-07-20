package com.aiinterview.backend.security.oauth;

import com.aiinterview.backend.security.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");

        if (email == null || email.isBlank()) {

            String errorUrl = "http://localhost:5173/login?oauthError="
                    + URLEncoder.encode(
                            "Unable to retrieve email.",
                            StandardCharsets.UTF_8
                    );

            getRedirectStrategy().sendRedirect(
                    request,
                    response,
                    errorUrl
            );

            return;
        }

        String token = JwtUtil.generateToken(email);

        String redirectUrl = "http://localhost:5173/login?token="
                + URLEncoder.encode(
                        token,
                        StandardCharsets.UTF_8
                );

        getRedirectStrategy().sendRedirect(
                request,
                response,
                redirectUrl
        );
    }
}