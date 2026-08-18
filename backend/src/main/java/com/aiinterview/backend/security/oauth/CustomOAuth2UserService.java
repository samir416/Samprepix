package com.aiinterview.backend.security.oauth;

import com.aiinterview.backend.entity.AccountStatus;
import com.aiinterview.backend.entity.AuthenticationProvider;
import com.aiinterview.backend.entity.GitHubConnection;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.entity.UserProfile;
import com.aiinterview.backend.repository.GitHubConnectionRepository;
import com.aiinterview.backend.repository.UserProfileRepository;
import com.aiinterview.backend.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService
        extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    private final UserProfileRepository userProfileRepository;

    private final GitHubEmailService gitHubEmailService;

    private final GitHubConnectionRepository
            gitHubConnectionRepository;

    public CustomOAuth2UserService(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            GitHubEmailService gitHubEmailService,
            GitHubConnectionRepository gitHubConnectionRepository
    ) {

        this.userRepository =
                userRepository;

        this.userProfileRepository =
                userProfileRepository;

        this.gitHubEmailService =
                gitHubEmailService;

        this.gitHubConnectionRepository =
                gitHubConnectionRepository;
    }

    @Override
    public OAuth2User loadUser(
            OAuth2UserRequest userRequest
    ) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User =
                super.loadUser(
                        userRequest
                );

        String registrationId =
                userRequest
                        .getClientRegistration()
                        .getRegistrationId();

        boolean github =
                "github".equalsIgnoreCase(
                        registrationId
                );

        String email;

        String name;

        String picture;

        String githubUsername = null;

        String githubAccessToken = null;

        if (github) {

            githubAccessToken =
                    userRequest
                            .getAccessToken()
                            .getTokenValue();

            email =
                    oAuth2User.getAttribute(
                            "email"
                    );

            if (email == null ||
                    email.isBlank()) {

                email =
                        gitHubEmailService
                                .getPrimaryEmail(
                                        githubAccessToken
                                );
            }

            name =
                    oAuth2User.getAttribute(
                            "name"
                    );

            githubUsername =
                    oAuth2User.getAttribute(
                            "login"
                    );

            if (name == null ||
                    name.isBlank()) {

                name =
                        githubUsername;
            }

            picture =
                    oAuth2User.getAttribute(
                            "avatar_url"
                    );

        } else {

            email =
                    oAuth2User.getAttribute(
                            "email"
                    );

            name =
                    oAuth2User.getAttribute(
                            "name"
                    );

            picture =
                    oAuth2User.getAttribute(
                            "picture"
                    );
        }

        if (email == null ||
                email.isBlank()) {

            throw new OAuth2AuthenticationException(
                    new OAuth2Error(
                            "invalid_email"
                    ),
                    "Unable to retrieve email from "
                            + registrationId.toUpperCase()
            );
        }

        Optional<User> existingUser =
                userRepository.findByEmail(
                        email
                );

        User user;

        if (existingUser.isPresent()) {

            user =
                    existingUser.get();

            if (name != null &&
                    !name.isBlank()) {

                user.setName(name);
            }

            if (
                    (
                            user.getProfilePicture() == null ||
                            user.getProfilePicture().isBlank()
                    ) &&
                    picture != null &&
                    !picture.isBlank()
            ) {

                user.setProfilePicture(
                        picture
                );
            }

            if (
                    user.getProvider() ==
                            AuthenticationProvider.EMAIL
            ) {

                if (github) {

                    user.setProvider(
                            AuthenticationProvider.GITHUB
                    );

                } else {

                    user.setProvider(
                            AuthenticationProvider.GOOGLE
                    );
                }
            }

            userRepository.save(user);

        } else {

            user =
                    createNewUser(
                            email,
                            name,
                            picture,
                            github
                    );
        }

        if (github) {

            saveGitHubConnection(
                    user,
                    githubUsername,
                    githubAccessToken
            );
        }

        Map<String, Object> attributes =
                new HashMap<>(
                        oAuth2User.getAttributes()
                );

        attributes.put(
                "email",
                email
        );

        attributes.put(
                "name",
                name
        );

        attributes.put(
                "picture",
                picture
        );

        if (githubUsername != null) {

            attributes.put(
                    "login",
                    githubUsername
            );
        }

        return new DefaultOAuth2User(
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_USER"
                        )
                ),
                attributes,
                "email"
        );
    }

    private User createNewUser(
            String email,
            String name,
            String picture,
            boolean github
    ) {

        User newUser =
                new User();

        newUser.setEmail(
                email
        );

        newUser.setName(
                name
        );

        newUser.setUsername(
                generateUsername(
                        name,
                        email
                )
        );

        newUser.setProfilePicture(
                picture
        );

        newUser.setEmailVerified(
                true
        );

        newUser.setAccountStatus(
                AccountStatus.ACTIVE
        );

        newUser.setProvider(
                github
                        ? AuthenticationProvider.GITHUB
                        : AuthenticationProvider.GOOGLE
        );

        User savedUser =
                userRepository.save(
                        newUser
                );

        UserProfile profile =
                new UserProfile();

        profile.setUser(
                savedUser
        );

        savedUser.setProfile(
                profile
        );

        userProfileRepository.save(
                profile
        );

        return savedUser;
    }

    private String generateUsername(
            String name,
            String email
    ) {

        String base;

        if (name != null &&
                !name.isBlank()) {

            base =
                    name
                            .trim()
                            .toLowerCase()
                            .replaceAll(
                                    "[^a-z0-9]",
                                    ""
                            );

        } else {

            base =
                    email
                            .split("@")[0]
                            .trim()
                            .toLowerCase()
                            .replaceAll(
                                    "[^a-z0-9]",
                                    ""
                            );
        }

        if (base.isBlank()) {

            base = "user";
        }

        String username =
                base;

        while (
                userRepository
                        .existsByUsername(
                                username
                        )
        ) {

            username =
                    base +
                    System.currentTimeMillis()
                            % 10000;
        }

        return username;
    }

    private void saveGitHubConnection(
            User user,
            String githubUsername,
            String accessToken
    ) {

        if (user == null ||
                accessToken == null ||
                accessToken.isBlank()) {

            return;
        }

        GitHubConnection connection =
                gitHubConnectionRepository
                        .findByUser(user)
                        .orElseGet(
                                GitHubConnection::new
                        );

        connection.setUser(
                user
        );

        if (githubUsername != null &&
                !githubUsername.isBlank()) {

            connection.setGithubUsername(
                    githubUsername
            );
        }

        connection.setAccessToken(
                accessToken
        );

        connection.setUpdatedAt(
                LocalDateTime.now()
        );

        if (connection.getConnectedAt() == null) {

            connection.setConnectedAt(
                    LocalDateTime.now()
            );
        }

        gitHubConnectionRepository.save(
                connection
        );
    }
}