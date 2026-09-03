package com.aiinterview.backend.controller;

import com.aiinterview.backend.entity.GitHubConnection;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.GitHubConnectionRepository;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.service.coding.GitHubRepositoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.List;

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
    private final GitHubRepositoryService gitHubRepositoryService;

    public GitHubProfileController(
            UserRepository userRepository,
            GitHubConnectionRepository gitHubConnectionRepository,
            GitHubRepositoryService gitHubRepositoryService
    ) {
        this.userRepository = userRepository;
        this.gitHubConnectionRepository =
                gitHubConnectionRepository;
        this.gitHubRepositoryService = gitHubRepositoryService;
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

        boolean connected = connection != null &&
            connection.getAccessToken() != null &&
            !connection.getAccessToken().isBlank();

        String repositoryUrl = connection == null ||
            connection.getRepositoryUrl() == null
            ? ""
            : connection.getRepositoryUrl();

        if (!connected && (connection == null ||
            connection.getRepositoryUrl() == null ||
            connection.getRepositoryUrl().isBlank())) {

            return ResponseEntity.ok(
                    Map.of(
                            "connected", false,
                            "repositoryUrl", "",
                            "pushAuthorized", false
                    )
            );
        }

        return ResponseEntity.ok(
                Map.of(
                        "connected", connected,
                        "repositoryUrl",
                        repositoryUrl,
                        "pushAuthorized", connected
                )
        );
    }

                @GetMapping("/repositories")
                public ResponseEntity<Map<String, Object>> getRepositories(
                    Authentication authentication
                ) {
                User user = getAuthenticatedUser(authentication);
                GitHubConnection connection = gitHubConnectionRepository
                    .findByUser(user)
                    .orElse(null);

                boolean connected = connection != null &&
                    connection.getAccessToken() != null &&
                    !connection.getAccessToken().isBlank();

                String repositoryUrl = connection == null ||
                    connection.getRepositoryUrl() == null
                    ? ""
                    : connection.getRepositoryUrl();

                if (!connected) {
                    return ResponseEntity.ok(Map.of(
                        "connected", false,
                        "repositories", List.of(),
                        "selectedRepositoryUrl", repositoryUrl,
                        "pushAuthorized", false
                    ));
                }

                return ResponseEntity.ok(Map.of(
                    "connected", true,
                    "repositories", gitHubRepositoryService.getRepositories(user),
                    "selectedRepositoryUrl", repositoryUrl,
                    "pushAuthorized", true
                ));
                }

                @PostMapping("/repository")
                public ResponseEntity<Map<String, Object>> saveRepository(
                    @RequestBody Map<String, Object> payload,
                    Authentication authentication
                ) {
                User user = getAuthenticatedUser(authentication);
                String repositoryUrl = payload == null
                    ? ""
                    : String.valueOf(payload.getOrDefault("repositoryUrl", ""));

                try {
                    GitHubConnection connection = gitHubConnectionRepository
                        .findByUser(user)
                        .orElse(null);

                    boolean authorized = connection != null &&
                        connection.getAccessToken() != null &&
                        !connection.getAccessToken().isBlank();

                    String savedUrl;
                    if (authorized) {
                    savedUrl = gitHubRepositoryService
                        .validateRepository(user, repositoryUrl)
                        .getRepositoryUrl();
                    } else {
                    savedUrl = gitHubRepositoryService
                        .saveRepositoryReference(user, repositoryUrl);
                    }

                    return ResponseEntity.ok(Map.of(
                        "connected", authorized,
                        "repositoryUrl", savedUrl,
                        "pushAuthorized", authorized,
                        "message", authorized
                            ? "Repository saved successfully."
                            : "Repository saved. Connect GitHub to enable automatic solution syncing."
                    ));
                } catch (IllegalArgumentException exception) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "message", "Enter a valid GitHub repository URL."
                    ));
                } catch (Exception exception) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "message", "Unable to validate that GitHub repository."
                    ));
                }
                }

                private User getAuthenticatedUser(Authentication authentication) {
                if (authentication == null || authentication.getName() == null ||
                    authentication.getName().isBlank()) {
                    throw new IllegalStateException("Authenticated user not found.");
                }

                return userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new IllegalStateException("User account not found."));
                }
}