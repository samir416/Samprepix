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

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_email"),
                    "Unable to retrieve email from Google."
            );
        }

        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isEmpty()) {

            throw new OAuth2AuthenticationException(
                    new OAuth2Error("account_not_found"),
                    "Account not found. Please register first."
            );
        }

        User user = existingUser.get();

        user.setName(name);

        if (picture != null && !picture.isBlank()) {
            user.setProfilePicture(picture);
        }

        if (user.getProvider() == AuthenticationProvider.EMAIL) {
            user.setProvider(AuthenticationProvider.GOOGLE);
        }

        userRepository.save(user);

        return oAuth2User;
    }
}