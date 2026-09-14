package com.aiinterview.backend.security.oauth;

import com.aiinterview.backend.entity.AccountStatus;
import com.aiinterview.backend.entity.AuthenticationProvider;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.entity.UserProfile;
import com.aiinterview.backend.repository.UserProfileRepository;
import com.aiinterview.backend.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    public CustomOidcUserService(UserRepository userRepository, UserProfileRepository userProfileRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional
    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        String picture = oidcUser.getPicture();

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("invalid_email");
        }

        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            if (name != null && !name.isBlank()) {
                user.setName(name);
            }
            if ((user.getProfilePicture() == null || user.getProfilePicture().isBlank()) && picture != null && !picture.isBlank()) {
                user.setProfilePicture(picture);
            }
            if (user.getProvider() == AuthenticationProvider.EMAIL) {
                user.setProvider(AuthenticationProvider.GOOGLE);
            }
            userRepository.saveAndFlush(user);
        } else {
            user = createNewUser(email, name, picture);
        }

        Map<String, Object> attributes = new HashMap<>(oidcUser.getAttributes());
        attributes.put("email", email);

        return new DefaultOidcUser(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                oidcUser.getIdToken(),
                oidcUser.getUserInfo(),
                "email"
        );
    }

    private User createNewUser(String email, String name, String picture) {
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setName(name);
        newUser.setUsername(generateUsername(name, email));
        newUser.setProfilePicture(picture);
        newUser.setEmailVerified(true);
        newUser.setAccountStatus(AccountStatus.ACTIVE);
        newUser.setProvider(AuthenticationProvider.GOOGLE);

        User savedUser = userRepository.saveAndFlush(newUser);

        UserProfile profile = new UserProfile();
        profile.setUser(savedUser);
        savedUser.setProfile(profile);
        userProfileRepository.saveAndFlush(profile);

        return savedUser;
    }

    private String generateUsername(String name, String email) {
        String base;
        if (name != null && !name.isBlank()) {
            base = name.trim().toLowerCase().replaceAll("[^a-z0-9]", "");
        } else {
            base = email.split("@")[0].trim().toLowerCase().replaceAll("[^a-z0-9]", "");
        }
        if (base.isBlank()) {
            base = "user";
        }
        if (base.length() > 40) {
            base = base.substring(0, 40);
        }
        String username = base;
        while (userRepository.existsByUsername(username)) {
            username = base + (System.currentTimeMillis() % 10000);
        }
        return username;
    }
}
