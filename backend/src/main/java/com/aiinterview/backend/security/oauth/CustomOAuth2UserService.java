package com.aiinterview.backend.security.oauth;

import com.aiinterview.backend.entity.AuthenticationProvider;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
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

        Optional<User> existingUser = userRepository.findByEmail(email);

        User user;

        if (existingUser.isPresent()) {

            user = existingUser.get();

            user.setProvider(AuthenticationProvider.GOOGLE);
            user.setName(name);

            if (picture != null) {
                user.setProfilePicture(picture);
            }

        } else {

            user = new User();

            user.setEmail(email);
            user.setUsername(generateUsername(email));
            user.setName(name);
            user.setProvider(AuthenticationProvider.GOOGLE);
            user.setProfilePicture(picture);

            // OAuth users don't have password
            user.setPassword(null);
        }

        userRepository.save(user);

        return oAuth2User;
    }

    private String generateUsername(String email) {

        String username = email.substring(0, email.indexOf("@"));

        int count = 1;

        while (userRepository.existsByUsername(username)) {

            username = email.substring(0, email.indexOf("@")) + count;
            count++;
        }

        return username;
    }
}