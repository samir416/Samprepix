package com.aiinterview.backend.service.coding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class CodingProblemGithubService {

    private final HttpClient httpClient;

    private final ObjectMapper objectMapper;

    public CodingProblemGithubService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public List<String> getRepositoryFiles(
            String repository
    ) {

        if (repository == null ||
                repository.isBlank()) {

            return List.of();
        }

        String url =
                "https://api.github.com/repos/"
                        + repository.trim()
                        + "/contents";

        try {

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header(
                                    "Accept",
                                    "application/vnd.github+json"
                            )
                            .header(
                                    "X-GitHub-Api-Version",
                                    "2022-11-28"
                            )
                            .GET()
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() != 200) {
                return List.of();
            }

            JsonNode root =
                    objectMapper.readTree(
                            response.body()
                    );

            List<String> files =
                    new ArrayList<>();

            if (!root.isArray()) {
                return files;
            }

            for (JsonNode node : root) {

                if ("file".equals(
                        node.path("type").asText()
                )) {

                    String path =
                            node.path("path").asText();

                    if (!path.isBlank()) {
                        files.add(path);
                    }
                }
            }

            return files;

        } catch (Exception exception) {

            return List.of();
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

        String url =
                "https://api.github.com/repos/"
                        + repository.trim()
                        + "/contents/"
                        + path;

        try {

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header(
                                    "Accept",
                                    "application/vnd.github+json"
                            )
                            .header(
                                    "X-GitHub-Api-Version",
                                    "2022-11-28"
                            )
                            .GET()
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

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

            return new String(
                    Base64.getMimeDecoder().decode(
                            encodedContent
                    ),
                    java.nio.charset.StandardCharsets.UTF_8
            );

        } catch (IOException |
                 InterruptedException |
                 IllegalArgumentException exception) {

            return "";
        }
    }
}