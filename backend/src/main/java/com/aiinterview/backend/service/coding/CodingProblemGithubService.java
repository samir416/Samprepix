package com.aiinterview.backend.service.coding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Service
public class CodingProblemGithubService {

    private static final int MAX_FILES = 20000;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CodingProblemGithubService() {
        this.httpClient =
                HttpClient.newBuilder()
                        .followRedirects(
                                HttpClient.Redirect.NORMAL
                        )
                        .build();

        this.objectMapper =
                new ObjectMapper();
    }

    public List<String> getRepositoryFiles(
            String repository
    ) {

        if (repository == null ||
                repository.isBlank()) {

            return Collections.emptyList();
        }

        String normalizedRepository =
                repository
                        .trim()
                        .replaceAll(
                                "^https?://github\\.com/",
                                ""
                        )
                        .replaceAll(
                                "/+$",
                                ""
                        );

        try {

            String branch =
                    getDefaultBranch(
                            normalizedRepository
                    );

            List<String> files =
                    new ArrayList<>();

            collectFilesRecursively(
                    normalizedRepository,
                    "",
                    branch,
                    files
            );

            return files;

        } catch (Exception exception) {

            return Collections.emptyList();
        }
    }

    private void collectFilesRecursively(
            String repository,
            String path,
            String branch,
            List<String> files
    ) throws IOException, InterruptedException {

        if (files.size() >= MAX_FILES) {
            return;
        }

        String url =
                "https://api.github.com/repos/"
                        + repository
                        + "/contents";

        if (!path.isBlank()) {
            url += "/" + path;
        }

        url += "?ref=" + branch;

        HttpResponse<String> response =
                sendGet(url);

        if (response.statusCode() != 200) {
            return;
        }

        JsonNode root =
                objectMapper.readTree(
                        response.body()
                );

        if (!root.isArray()) {
            return;
        }

        for (JsonNode node : root) {

            if (files.size() >= MAX_FILES) {
                return;
            }

            String type =
                    node.path("type").asText();

            String itemPath =
                    node.path("path").asText();

            if (itemPath.isBlank()) {
                continue;
            }

            if ("file".equals(type)) {

                if (isPotentialProblemFile(
                        itemPath
                )) {

                    files.add(itemPath);
                }

                continue;
            }

            if ("dir".equals(type)) {

                collectFilesRecursively(
                        repository,
                        itemPath,
                        branch,
                        files
                );
            }
        }
    }

    public String getFileContent(
            String repository,
            String path
    ) {

        if (repository == null ||
                repository.isBlank() ||
                path == null ||
                path.isBlank()) {

            return "";
        }

        String normalizedRepository =
                repository
                        .trim()
                        .replaceAll(
                                "^https?://github\\.com/",
                                ""
                        )
                        .replaceAll(
                                "/+$",
                                ""
                        );

        String normalizedPath =
                path
                        .trim()
                        .replaceFirst(
                                "^/+",
                                ""
                        );

        String url =
                "https://api.github.com/repos/"
                        + normalizedRepository
                        + "/contents/"
                        + normalizedPath;

        try {

            HttpResponse<String> response =
                    sendGet(url);

            if (response.statusCode() != 200) {
                return "";
            }

            JsonNode root =
                    objectMapper.readTree(
                            response.body()
                    );

            String encodedContent =
                    root.path("content").asText();

            if (encodedContent.isBlank()) {
                return "";
            }

            String cleanContent =
                    encodedContent
                            .replaceAll(
                                    "\\s+",
                                    ""
                            );

            return new String(
                    Base64.getDecoder().decode(
                            cleanContent
                    ),
                    StandardCharsets.UTF_8
            );

        } catch (
                IOException |
                InterruptedException |
                IllegalArgumentException exception
        ) {

            return "";
        }
    }

    private String getDefaultBranch(
            String repository
    ) throws IOException, InterruptedException {

        String url =
                "https://api.github.com/repos/"
                        + repository;

        HttpResponse<String> response =
                sendGet(url);

        if (response.statusCode() != 200) {

            return "main";
        }

        JsonNode root =
                objectMapper.readTree(
                        response.body()
                );

        String branch =
                root.path(
                        "default_branch"
                ).asText();

        if (branch == null ||
                branch.isBlank()) {

            return "main";
        }

        return branch;
    }

    private HttpResponse<String> sendGet(
            String url
    ) throws IOException, InterruptedException {

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(url)
                        )
                        .header(
                                "Accept",
                                "application/vnd.github+json"
                        )
                        .header(
                                "X-GitHub-Api-Version",
                                "2022-11-28"
                        )
                        .header(
                                "User-Agent",
                                "AI-Placement-Platform"
                        )
                        .GET()
                        .build();

        return httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
    }

    private boolean isPotentialProblemFile(
            String path
    ) {

        String lowerPath =
                path.toLowerCase();

        if (
                lowerPath.contains("/.git/") ||
                lowerPath.startsWith(".git/")
        ) {

            return false;
        }

        if (
                lowerPath.contains(
                        "/node_modules/"
                ) ||
                lowerPath.contains(
                        "/target/"
                ) ||
                lowerPath.contains(
                        "/build/"
                ) ||
                lowerPath.contains(
                        "/dist/"
                )
        ) {

            return false;
        }

        return lowerPath.endsWith(".json") ||
                lowerPath.endsWith(".md") ||
                lowerPath.endsWith(".txt") ||
                lowerPath.endsWith(".yaml") ||
                lowerPath.endsWith(".yml") ||
                lowerPath.endsWith(".java") ||
                lowerPath.endsWith(".py") ||
                lowerPath.endsWith(".js") ||
                lowerPath.endsWith(".ts") ||
                lowerPath.endsWith(".cpp") ||
                lowerPath.endsWith(".c") ||
                lowerPath.endsWith(".cs") ||
                lowerPath.endsWith(".go") ||
                lowerPath.endsWith(".rs");
    }
}