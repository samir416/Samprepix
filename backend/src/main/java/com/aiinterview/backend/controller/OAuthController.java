package com.aiinterview.backend.controller;

import com.aiinterview.backend.entity.GitHubConnection;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.GitHubConnectionRepository;
import com.aiinterview.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Controller
public class OAuthController {

    @GetMapping("/oauth2/login/google")
    public String googleLogin() {
        return "redirect:/oauth2/authorize/google";
    }

    @GetMapping("/oauth2/login/github")
    public String githubLogin() {
        return "redirect:/oauth2/authorize/github";
    }

    @GetMapping("/oauth2/register/google")
    public String googleRegister() {
        return "redirect:/oauth2/authorize/google";
    }

    @GetMapping("/oauth2/register/github")
    public String githubRegister() {
        return "redirect:/oauth2/authorize/github";
    }
}

@RestController
@RequestMapping("/api/github")
class GitHubProfileController {

    private final UserRepository userRepository;
    private final GitHubConnectionRepository gitHubConnectionRepository;

    public GitHubProfileController(
            UserRepository userRepository,
            GitHubConnectionRepository gitHubConnectionRepository
    ) {
        this.userRepository = userRepository;
        this.gitHubConnectionRepository =
                gitHubConnectionRepository;
    }

    @GetMapping("/repository")
    public ResponseEntity<Map<String, Object>> getRepository(
            Authentication authentication
    ) {

        if (authentication == null ||
                authentication.getName() == null ||
                authentication.getName().isBlank()) {

            return ResponseEntity.status(401).build();
        }

        User user =
                userRepository
                        .findByEmail(authentication.getName())
                        .orElse(null);

        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        GitHubConnection connection =
                gitHubConnectionRepository
                        .findByUser(user)
                        .orElse(null);

        if (connection == null ||
                connection.getRepositoryUrl() == null ||
                connection.getRepositoryUrl().isBlank()) {

            return ResponseEntity.ok(
                    Map.of(
                            "connected", false,
                            "repositoryUrl", ""
                    )
            );
        }

        return ResponseEntity.ok(
                Map.of(
                        "connected", true,
                        "repositoryUrl",
                        connection.getRepositoryUrl()
                )
        );
    }
}