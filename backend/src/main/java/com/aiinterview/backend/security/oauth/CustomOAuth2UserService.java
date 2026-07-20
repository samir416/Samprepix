package com.aiinterview.backend.security.oauth;

import com.aiinterview.backend.entity.AuthenticationProvider;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final GitHubEmailService gitHubEmailService;

    public CustomOAuth2UserService(
            UserRepository userRepository,
            GitHubEmailService gitHubEmailService
    ) {
        this.userRepository = userRepository;
        this.gitHubEmailService = gitHubEmailService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId =
                userRequest.getClientRegistration()
                        .getRegistrationId();

        String email;
        String name;
        String picture;

        if ("github".equalsIgnoreCase(registrationId)) {

            email = oAuth2User.getAttribute("email");

            // GitHub often returns null email
            if (email == null || email.isBlank()) {

                String accessToken =
                        userRequest.getAccessToken().getTokenValue();

                email =
                        gitHubEmailService.getPrimaryEmail(accessToken);
            }

            name = oAuth2User.getAttribute("name");

            if (name == null || name.isBlank()) {
                name = oAuth2User.getAttribute("login");
            }

            picture = oAuth2User.getAttribute("avatar_url");

        } else {

            // Google

            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
            picture = oAuth2User.getAttribute("picture");
        }

        if (email == null || email.isBlank()) {

            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_email"),
                    "Unable to retrieve email from "
                            + registrationId.toUpperCase()
            );
        }

        Optional<User> existingUser =
                userRepository.findByEmail(email);

        if (existingUser.isEmpty()) {

            throw new OAuth2AuthenticationException(
                    new OAuth2Error("account_not_found"),
                    "Account not found. Please register first."
            );
        }

        User user = existingUser.get();

        if (name != null && !name.isBlank()) {
            user.setName(name);
        }

        if (picture != null && !picture.isBlank()) {
            user.setProfilePicture(picture);
        }

        if (user.getProvider() == AuthenticationProvider.EMAIL) {

            if ("github".equalsIgnoreCase(registrationId)) {
                user.setProvider(AuthenticationProvider.GITHUB);
            } else {
                user.setProvider(AuthenticationProvider.GOOGLE);
            }
        }

        userRepository.save(user);

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());

attributes.put("email", email);
attributes.put("name", name);
attributes.put("picture", picture);

return new DefaultOAuth2User(
        List.of(new SimpleGrantedAuthority("ROLE_USER")),
        attributes,
        "email"
);
    }
}