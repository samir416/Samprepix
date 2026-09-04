package com.aiinterview.backend.service.coding;

import com.aiinterview.backend.config.CentralLanguageRegistry;
import com.aiinterview.backend.dto.coding.GitHubSyncResult;
import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.entity.GitHubConnection;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.GitHubConnectionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GitHubRepositoryService {

    private static final String GITHUB_API =
            "https://api.github.com";

    private static final String GITHUB_API_VERSION =
            "2022-11-28";

    private static final Pattern REPOSITORY_PATTERN =
            Pattern.compile(
                    "^https?://github\\.com/([^/]+)/([^/#?]+?)/?$",
                    Pattern.CASE_INSENSITIVE
            );

    private final GitHubConnectionRepository gitHubConnectionRepository;

    private final HttpClient httpClient;

    private final ObjectMapper objectMapper;

    public GitHubRepositoryService(
            GitHubConnectionRepository gitHubConnectionRepository
    ) {
        this.gitHubConnectionRepository =
                gitHubConnectionRepository;

        this.httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(
                                Duration.ofSeconds(10)
                        )
                        .build();

        this.objectMapper =
                new ObjectMapper();
    }

    public GitHubRepositoryResult validateRepository(
            User user,
            String repositoryUrl
    ) {

        GitHubConnection connection =
                getConnection(user, true);

        RepositoryReference repository =
                parseRepositoryUrl(repositoryUrl);

        JsonNode repositoryData =
                sendRepositoryRequest(
                        connection.getAccessToken(),
                        repository
                );

        boolean privateRepository =
                repositoryData
                        .path("private")
                        .asBoolean(false);

        boolean pushPermission =
                repositoryData
                        .path("permissions")
                        .path("push")
                        .asBoolean(false);

        if (!pushPermission) {

            throw new IllegalStateException(
                    "GitHub account does not have write access to this repository."
            );
        }

        String htmlUrl =
                repositoryData
                        .path("html_url")
                        .asText(
                                repositoryUrl
                        );

        String ownerLogin =
                repositoryData
                        .path("owner")
                        .path("login")
                        .asText("");

        if (ownerLogin.isBlank()) {
            ownerLogin = repository.owner();
        }

        connection.setGithubUsername(
                ownerLogin
        );

        connection.setRepositoryUrl(
                normalizeRepositoryUrl(
                                "https://github.com/" +
                                                repository.owner() + "/" +
                                                repository.repository()
                )
        );

        gitHubConnectionRepository.save(
                connection
        );

        return GitHubRepositoryResult.builder()
                .owner(
                        repository.owner()
                )
                .repository(
                        repository.repository()
                )
                .repositoryUrl(
                        normalizeRepositoryUrl(
                                htmlUrl
                        )
                )
                .privateRepository(
                        privateRepository
                )
                .pushPermission(
                        pushPermission
                )
                .build();
    }

        public List<Map<String, Object>> getRepositories(User user) {
                GitHubConnection connection = getConnection(user, false);
                return fetchUserRepositories(connection.getAccessToken());
        }

        public String saveRepositoryReference(User user, String repositoryUrl) {
                RepositoryReference repository = parseRepositoryUrl(repositoryUrl);
                String normalizedRepositoryUrl = normalizeRepositoryUrl(
                                "https://github.com/" +
                                                repository.owner() + "/" +
                                                repository.repository()
                );

                GitHubConnection connection = gitHubConnectionRepository
                                .findByUser(user)
                                .orElseGet(GitHubConnection::new);

                connection.setUser(user);
                connection.setRepositoryUrl(normalizedRepositoryUrl);
                gitHubConnectionRepository.save(connection);

                return normalizedRepositoryUrl;
        }

    public GitHubPushResult pushSolution(
            User user,
            String repositoryUrl,
            String filePath,
            String sourceCode,
            String commitMessage
    ) {

        if (
                filePath == null ||
                filePath.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "GitHub solution file path is required."
            );
        }

        if (
                sourceCode == null ||
                sourceCode.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Solution code cannot be empty."
            );
        }

        if (
                commitMessage == null ||
                commitMessage.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "GitHub commit message is required."
            );
        }

        GitHubConnection connection =
                getConnection(user, true);

        RepositoryReference repository =
                parseRepositoryUrl(repositoryUrl);

        JsonNode repositoryData =
                sendRepositoryRequest(
                        connection.getAccessToken(),
                        repository
                );

        boolean pushPermission =
                repositoryData
                        .path("permissions")
                        .path("push")
                        .asBoolean(false);

        if (!pushPermission) {

            throw new IllegalStateException(
                    "GitHub account does not have write access to this repository."
            );
        }

        String normalizedPath =
                normalizeFilePath(filePath);

        String existingSha =
                getExistingFileSha(
                        connection.getAccessToken(),
                        repository,
                        normalizedPath
                );

        String encodedContent =
                Base64.getEncoder()
                        .encodeToString(
                                sourceCode.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        );

        Map<String, Object> payload =
                new LinkedHashMap<>();

        payload.put(
                "message",
                commitMessage.trim()
        );

        payload.put(
                "content",
                encodedContent
        );

        if (
                existingSha != null &&
                !existingSha.isBlank()
        ) {

            payload.put(
                    "sha",
                    existingSha
            );
        }

        String branch =
                repositoryData
                        .path("default_branch")
                        .asText("main")
                        .trim();

        if (branch.isBlank()) {
            branch = "main";
        }

        payload.put(
                "branch",
                branch
        );

        String requestBody;

        try {

            requestBody =
                    objectMapper.writeValueAsString(
                            payload
                    );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to prepare GitHub solution payload.",
                    exception
            );
        }

        String endpoint =
                GITHUB_API +
                "/repos/" +
                repository.owner() +
                "/" +
                repository.repository() +
                "/contents/" +
                normalizedPath;

        HttpRequest request =
                githubRequest(
                        connection.getAccessToken(),
                        endpoint
                )
                        .PUT(
                                HttpRequest.BodyPublishers
                                        .ofString(
                                                requestBody
                                        )
                        )
                        .build();

        try {

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers
                                    .ofString()
                    );

            if (
                    response.statusCode() != 200 &&
                    response.statusCode() != 201
            ) {

                throw githubApiException(
                        response,
                        "Unable to push coding solution to GitHub."
                );
            }

            if (
                    response.body() == null ||
                    response.body().isBlank()
            ) {

                throw new IllegalStateException(
                        "GitHub returned an empty response after pushing the solution."
                );
            }

            JsonNode root =
                    objectMapper.readTree(
                            response.body()
                    );

            String commitSha =
                    root.path("commit")
                            .path("sha")
                            .asText("");

            String commitUrl =
                    root.path("commit")
                            .path("html_url")
                            .asText("");

            String fileUrl =
                    root.path("content")
                            .path("html_url")
                            .asText("");

            String normalizedRepositoryUrl =
                    normalizeRepositoryUrl(
                            repositoryUrl
                    );

            connection.setRepositoryUrl(
                    normalizedRepositoryUrl
            );

            gitHubConnectionRepository.save(
                    connection
            );

            return GitHubPushResult.builder()
                    .success(true)
                    .repositoryUrl(
                            normalizedRepositoryUrl
                    )
                    .filePath(
                            normalizedPath
                    )
                    .commitSha(
                            commitSha
                    )
                    .commitUrl(
                            commitUrl
                    )
                    .fileUrl(
                            fileUrl
                    )
                    .message(
                            "Solution pushed to GitHub successfully."
                    )
                    .build();

        } catch (
                java.io.IOException |
                InterruptedException exception
        ) {

            if (
                    exception instanceof InterruptedException
            ) {

                Thread.currentThread()
                        .interrupt();
            }

            throw new IllegalStateException(
                    "Unable to connect to GitHub.",
                    exception
            );
        }
    }

    public GitHubPushResult syncSolution(
            User user,
            String repositoryUrl,
            CodingProblem problem,
            String language,
            String sourceCode
    ) {
        if (problem == null) {
            throw new IllegalArgumentException("Coding problem is required.");
        }
        if (language == null || language.isBlank()) {
            throw new IllegalArgumentException("Programming language is required.");
        }
        if (sourceCode == null || sourceCode.isBlank()) {
            throw new IllegalArgumentException("Solution code cannot be empty.");
        }

        GitHubConnection connection = getConnection(user, true);
        RepositoryReference repository = parseRepositoryUrl(repositoryUrl);

        JsonNode repositoryData = sendRepositoryRequest(
                connection.getAccessToken(),
                repository
        );

        boolean pushPermission = repositoryData
                .path("permissions")
                .path("push")
                .asBoolean(false);

        if (!pushPermission) {
            throw new IllegalStateException(
                    "GitHub account does not have write access to this repository."
            );
        }

        String branch = repositoryData
                .path("default_branch")
                .asText("main")
                .trim();
        if (branch.isBlank()) {
            branch = "main";
        }

        String filePath = GitHubSolutionHelper.getSolutionPath(problem, language);
        String normalizedPath = normalizeFilePath(filePath);

        CentralLanguageRegistry.LanguageSpec spec = CentralLanguageRegistry.get(language);
        String langDisplay = spec != null ? spec.displayName() : language;

        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            ExistingFile existingFile = getExistingFile(
                    connection.getAccessToken(),
                    repository,
                    normalizedPath
            );

            String existingContent = existingFile != null ? existingFile.content() : "";
            String existingSha = existingFile != null ? existingFile.sha() : null;

            GitHubSolutionHelper.MergeResult mergeResult =
                    GitHubSolutionHelper.prepareMergedContent(sourceCode, existingContent, language);

            String repoNormalizedUrl = normalizeRepositoryUrl(repositoryUrl);
            String fileUrl = repoNormalizedUrl + "/blob/" + branch + "/" + normalizedPath;

            if (mergeResult.duplicate()) {
                return GitHubPushResult.builder()
                        .success(true)
                        .alreadySynced(true)
                        .solutionNumber(mergeResult.solutionNumber())
                        .repositoryUrl(repoNormalizedUrl)
                        .filePath(normalizedPath)
                        .fileUrl(fileUrl)
                        .message("Solution already synced to GitHub.")
                        .build();
            }

            String commitMessage;
            if (mergeResult.solutionNumber() == 1) {
                commitMessage = "Add " + problem.getTitle() + " " + langDisplay + " solution";
            } else {
                commitMessage = "Add Solution " + mergeResult.solutionNumber() + " for " + problem.getTitle() + " in " + langDisplay;
            }

            String encodedContent = Base64.getEncoder().encodeToString(
                    mergeResult.content().getBytes(StandardCharsets.UTF_8)
            );

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("message", commitMessage.trim());
            payload.put("content", encodedContent);
            if (existingSha != null && !existingSha.isBlank()) {
                payload.put("sha", existingSha);
            }
            payload.put("branch", branch);

            String requestBody;
            try {
                requestBody = objectMapper.writeValueAsString(payload);
            } catch (Exception e) {
                throw new IllegalStateException("Unable to prepare GitHub solution payload.", e);
            }

            String endpoint = GITHUB_API + "/repos/" + repository.owner() + "/" + repository.repository() + "/contents/" + normalizedPath;
            HttpRequest request = githubRequest(connection.getAccessToken(), endpoint)
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 409) {
                    if (attempt < maxAttempts) {
                        try {
                            Thread.sleep(100L * attempt);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                        continue;
                    }
                    throw githubApiException(response, "Conflict updating GitHub solution file.");
                }

                if (response.statusCode() != 200 && response.statusCode() != 201) {
                    throw githubApiException(response, "Unable to push coding solution to GitHub.");
                }

                JsonNode root = objectMapper.readTree(response.body());
                String commitSha = root.path("commit").path("sha").asText("");
                String commitUrl = root.path("commit").path("html_url").asText("");
                String respFileUrl = root.path("content").path("html_url").asText(fileUrl);

                connection.setRepositoryUrl(repoNormalizedUrl);
                gitHubConnectionRepository.save(connection);

                String successMessage = mergeResult.solutionNumber() == 1
                        ? "Solution pushed to GitHub successfully."
                        : "Solution " + mergeResult.solutionNumber() + " synced to GitHub.";

                return GitHubPushResult.builder()
                        .success(true)
                        .alreadySynced(false)
                        .solutionNumber(mergeResult.solutionNumber())
                        .repositoryUrl(repoNormalizedUrl)
                        .filePath(normalizedPath)
                        .commitSha(commitSha)
                        .commitUrl(commitUrl)
                        .fileUrl(respFileUrl)
                        .message(successMessage)
                        .build();
            } catch (java.io.IOException | InterruptedException exception) {
                if (exception instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                throw new IllegalStateException("Unable to connect to GitHub.", exception);
            }
        }

        throw new IllegalStateException("Unable to synchronize solution to GitHub after retries.");
    }

    private GitHubConnection getConnection(User user, boolean requireToken) {

        if (
                user == null ||
                user.getId() == null
        ) {

            throw new IllegalArgumentException(
                    "Authenticated user is required."
            );
        }

        GitHubConnection connection =
                gitHubConnectionRepository
                        .findByUser(user)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "GitHub account is not connected."
                                        )
                        );

        if (requireToken && (
                connection.getAccessToken() == null ||
                connection.getAccessToken().isBlank()
        )) {

            throw new IllegalStateException(
                    "GitHub authorization is missing. Please reconnect GitHub."
            );
        }

        return connection;
    }

        private List<Map<String, Object>> fetchUserRepositories(String accessToken) {
                String endpoint = GITHUB_API + "/user/repos?per_page=100&sort=updated";
                HttpRequest request = githubRequest(accessToken, endpoint).GET().build();

                try {
                        HttpResponse<String> response = httpClient.send(
                                        request,
                                        HttpResponse.BodyHandlers.ofString()
                        );

                        if (response.statusCode() != 200) {
                                throw githubApiException(
                                                response,
                                                "Unable to load your GitHub repositories."
                                );
                        }

                        JsonNode root = objectMapper.readTree(response.body());
                        List<Map<String, Object>> repositories = new ArrayList<>();

                        if (root.isArray()) {
                                for (JsonNode item : root) {
                                        repositories.add(Map.of(
                                                        "id", item.path("id").asLong(0),
                                                        "name", item.path("name").asText(""),
                                                        "fullName", item.path("full_name").asText(""),
                                                        "htmlUrl", item.path("html_url").asText(""),
                                                        "private", item.path("private").asBoolean(false),
                                                        "defaultBranch", item.path("default_branch").asText("main")
                                        ));
                                }
                        }

                        return repositories;
                } catch (java.io.IOException | InterruptedException exception) {
                        if (exception instanceof InterruptedException) {
                                Thread.currentThread().interrupt();
                        }

                        throw new IllegalStateException(
                                        "Unable to connect to GitHub.",
                                        exception
                        );
                }
        }

    private RepositoryReference parseRepositoryUrl(
            String repositoryUrl
    ) {

        if (
                repositoryUrl == null ||
                repositoryUrl.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "GitHub repository URL is required."
            );
        }

        String normalized =
                repositoryUrl
                        .trim()
                        .replaceAll(
                                "/+$",
                                ""
                        );

        Matcher matcher =
                REPOSITORY_PATTERN.matcher(
                        normalized
                );

        if (!matcher.matches()) {

            throw new IllegalArgumentException(
                    "Invalid GitHub repository URL."
            );
        }

        String owner =
                matcher.group(1)
                        .trim();

        String repository =
                matcher.group(2)
                        .trim()
                        .replaceAll(
                                "\\.git$",
                                ""
                        );

        if (
                owner.isBlank() ||
                repository.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Invalid GitHub repository URL."
            );
        }

        return new RepositoryReference(
                owner,
                repository
        );
    }

    private JsonNode sendRepositoryRequest(
            String accessToken,
            RepositoryReference repository
    ) {

        if (
                accessToken == null ||
                accessToken.isBlank()
        ) {

            throw new IllegalStateException(
                    "GitHub authorization is missing."
            );
        }

        String endpoint =
                GITHUB_API +
                "/repos/" +
                repository.owner() +
                "/" +
                repository.repository();

        HttpRequest request =
                githubRequest(
                        accessToken,
                        endpoint
                )
                        .GET()
                        .build();

        try {

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers
                                    .ofString()
                    );

            if (response.statusCode() != 200) {

                throw githubApiException(
                        response,
                        "Unable to access GitHub repository."
                );
            }

            if (
                    response.body() == null ||
                    response.body().isBlank()
            ) {

                throw new IllegalStateException(
                        "GitHub returned an empty repository response."
                );
            }

            return objectMapper.readTree(
                    response.body()
            );

        } catch (
                java.io.IOException |
                InterruptedException exception
        ) {

            if (
                    exception instanceof InterruptedException
            ) {

                Thread.currentThread()
                        .interrupt();
            }

            throw new IllegalStateException(
                    "Unable to connect to GitHub.",
                    exception
            );
        }
    }

    public ExistingFile getExistingFile(
            String accessToken,
            RepositoryReference repository,
            String filePath
    ) {
        String endpoint =
                GITHUB_API +
                "/repos/" +
                repository.owner() +
                "/" +
                repository.repository() +
                "/contents/" +
                filePath;

        HttpRequest request =
                githubRequest(
                        accessToken,
                        endpoint
                )
                        .GET()
                        .build();

        try {
            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers
                                    .ofString()
                    );

            if (response.statusCode() == 404) {
                return null;
            }

            if (response.statusCode() != 200) {
                throw githubApiException(
                        response,
                        "Unable to check existing GitHub solution file."
                );
            }

            if (response.body() == null || response.body().isBlank()) {
                return null;
            }

            JsonNode root =
                    objectMapper.readTree(
                            response.body()
                    );

            String sha =
                    root.path(
                            "sha"
                    ).asText("");

            if (sha.isBlank()) {
                return null;
            }

            String encodedContent = root.path("content").asText("");
            String content = "";
            if (!encodedContent.isBlank()) {
                try {
                    byte[] decoded = Base64.getDecoder().decode(
                            encodedContent.replaceAll("\\s+", "")
                    );
                    content = new String(decoded, StandardCharsets.UTF_8);
                } catch (Exception ignored) {
                    content = "";
                }
            }

            return new ExistingFile(sha, content);

        } catch (
                java.io.IOException |
                InterruptedException exception
        ) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            throw new IllegalStateException(
                    "Unable to check GitHub solution file.",
                    exception
            );
        }
    }

    private String getExistingFileSha(
            String accessToken,
            RepositoryReference repository,
            String filePath
    ) {
        ExistingFile file = getExistingFile(accessToken, repository, filePath);
        return file != null ? file.sha() : null;
    }

    private HttpRequest.Builder githubRequest(
            String accessToken,
            String endpoint
    ) {

        return HttpRequest.newBuilder()
                .uri(
                        URI.create(
                                endpoint
                        )
                )
                .timeout(
                        Duration.ofSeconds(30)
                )
                .header(
                        "Accept",
                        "application/vnd.github+json"
                )
                .header(
                        "Authorization",
                        "Bearer " +
                                accessToken
                )
                .header(
                        "X-GitHub-Api-Version",
                        GITHUB_API_VERSION
                );
    }

    private IllegalStateException githubApiException(
            HttpResponse<String> response,
            String fallbackMessage
    ) {

        String message =
                fallbackMessage;

        try {

            if (
                    response.body() != null &&
                    !response.body().isBlank()
            ) {

                JsonNode root =
                        objectMapper.readTree(
                                response.body()
                        );

                String githubMessage =
                        root.path(
                                "message"
                        ).asText("");

                if (
                        !githubMessage.isBlank()
                ) {

                    message =
                            githubMessage;
                }
            }

        } catch (Exception ignored) {
        }

        if (
                response.statusCode() == 401
        ) {

            message =
                    "GitHub authorization has expired. Please reconnect GitHub.";
        }

        if (
                response.statusCode() == 403
        ) {

            message =
                    "GitHub denied repository access or write permission.";
        }

        if (
                response.statusCode() == 404
        ) {

            message =
                    "GitHub repository or file was not found or is not accessible.";
        }

        return new IllegalStateException(
                message
        );
    }

    private String normalizeRepositoryUrl(
            String repositoryUrl
    ) {

        RepositoryReference repository =
                parseRepositoryUrl(
                        repositoryUrl
                );

        return "https://github.com/" +
                repository.owner() +
                "/" +
                repository.repository();
    }

    private String normalizeFilePath(
            String filePath
    ) {

        String normalized =
                filePath
                        .trim()
                        .replace(
                                "\\",
                                "/"
                        )
                        .replaceAll(
                                "^/+",
                                ""
                        );

        if (
                normalized.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "GitHub solution file path is invalid."
            );
        }

        String[] segments =
                normalized.split("/");

        for (
                String segment :
                segments
        ) {

            if (
                    segment.isBlank() ||
                    ".".equals(segment) ||
                    "..".equals(segment)
            ) {

                throw new IllegalArgumentException(
                        "GitHub solution file path is invalid."
                );
            }
        }

        return normalized;
    }

    private record RepositoryReference(
            String owner,
            String repository
    ) {
    }

    public record ExistingFile(
            String sha,
            String content
    ) {
    }

    public static class GitHubRepositoryResult {

        private String owner;

        private String repository;

        private String repositoryUrl;

        private boolean privateRepository;

        private boolean pushPermission;

        public static Builder builder() {
            return new Builder();
        }

        public String getOwner() {
            return owner;
        }

        public String getRepository() {
            return repository;
        }

        public String getRepositoryUrl() {
            return repositoryUrl;
        }

        public boolean isPrivateRepository() {
            return privateRepository;
        }

        public boolean isPushPermission() {
            return pushPermission;
        }

        public static class Builder {

            private final GitHubRepositoryResult result =
                    new GitHubRepositoryResult();

            public Builder owner(
                    String owner
            ) {
                result.owner = owner;
                return this;
            }

            public Builder repository(
                    String repository
            ) {
                result.repository = repository;
                return this;
            }

            public Builder repositoryUrl(
                    String repositoryUrl
            ) {
                result.repositoryUrl =
                        repositoryUrl;
                return this;
            }

            public Builder privateRepository(
                    boolean privateRepository
            ) {
                result.privateRepository =
                        privateRepository;
                return this;
            }

            public Builder pushPermission(
                    boolean pushPermission
            ) {
                result.pushPermission =
                        pushPermission;
                return this;
            }

            public GitHubRepositoryResult build() {
                return result;
            }
        }
    }

    public static class GitHubPushResult {

        private boolean success;

        private boolean alreadySynced;

        private Integer solutionNumber;

        private String repositoryUrl;

        private String filePath;

        private String commitSha;

        private String commitUrl;

        private String fileUrl;

        private String message;

        public static Builder builder() {
            return new Builder();
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean isAlreadySynced() {
            return alreadySynced;
        }

        public Integer getSolutionNumber() {
            return solutionNumber;
        }

        public String getRepositoryUrl() {
            return repositoryUrl;
        }

        public String getFilePath() {
            return filePath;
        }

        public String getCommitSha() {
            return commitSha;
        }

        public String getCommitUrl() {
            return commitUrl;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        public String getMessage() {
            return message;
        }

        public GitHubSyncResult toSyncResult() {
            return GitHubSyncResult.builder()
                    .connected(true)
                    .synced(success && !alreadySynced)
                    .alreadySynced(alreadySynced)
                    .solutionNumber(solutionNumber)
                    .repositoryUrl(repositoryUrl)
                    .filePath(filePath)
                    .commitSha(commitSha)
                    .commitUrl(commitUrl)
                    .fileUrl(fileUrl)
                    .message(message)
                    .build();
        }

        public static class Builder {

            private final GitHubPushResult result =
                    new GitHubPushResult();

            public Builder success(
                    boolean success
            ) {
                result.success = success;
                return this;
            }

            public Builder alreadySynced(
                    boolean alreadySynced
            ) {
                result.alreadySynced = alreadySynced;
                return this;
            }

            public Builder solutionNumber(
                    Integer solutionNumber
            ) {
                result.solutionNumber = solutionNumber;
                return this;
            }

            public Builder repositoryUrl(
                    String repositoryUrl
            ) {
                result.repositoryUrl =
                        repositoryUrl;
                return this;
            }

            public Builder filePath(
                    String filePath
            ) {
                result.filePath =
                        filePath;
                return this;
            }

            public Builder commitSha(
                    String commitSha
            ) {
                result.commitSha =
                        commitSha;
                return this;
            }

            public Builder commitUrl(
                    String commitUrl
            ) {
                result.commitUrl =
                        commitUrl;
                return this;
            }

            public Builder fileUrl(
                    String fileUrl
            ) {
                result.fileUrl =
                        fileUrl;
                return this;
            }

            public Builder message(
                    String message
            ) {
                result.message =
                        message;
                return this;
            }

            public GitHubPushResult build() {
                return result;
            }
        }
    }
}