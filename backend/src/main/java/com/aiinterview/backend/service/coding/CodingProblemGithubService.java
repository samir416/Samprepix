package com.aiinterview.backend.service.coding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CodingProblemGithubService {

    private static final int MAX_FILES = 20000;

    private static final Duration CONNECT_TIMEOUT =
            Duration.ofSeconds(10);

    private static final Duration REQUEST_TIMEOUT =
            Duration.ofSeconds(30);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CodingProblemGithubService(
            ObjectMapper objectMapper
    ) {

        this.httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(
                                CONNECT_TIMEOUT
                        )
                        .followRedirects(
                                HttpClient.Redirect.NORMAL
                        )
                        .build();

        this.objectMapper =
                objectMapper;
    }

    public List<String> getRepositoryFiles(
            String repository
    ) {

        String normalizedRepository =
                normalizeRepository(
                        repository
                );

        if (
                normalizedRepository.isBlank() ||
                !isValidRepository(
                        normalizedRepository
                )
        ) {

            return Collections.emptyList();
        }

        try {

            String branch =
                    getDefaultBranch(
                            normalizedRepository
                    );

            String url =
                    "https://api.github.com/repos/"
                            + normalizedRepository
                            + "/git/trees/"
                            + encodeBranch(branch)
                            + "?recursive=1";

            HttpResponse<String> response =
                    sendGet(url);

            if (
                    response.statusCode() != 200
            ) {

                return Collections.emptyList();
            }

            if (
                    response.body() == null ||
                    response.body().isBlank()
            ) {

                return Collections.emptyList();
            }

            JsonNode root =
                    objectMapper.readTree(
                            response.body()
                    );

            JsonNode tree =
                    root.path("tree");

            if (!tree.isArray()) {
                return Collections.emptyList();
            }

            List<String> files =
                    new ArrayList<>();

            for (
                    JsonNode node :
                    tree
            ) {

                if (
                        files.size() >= MAX_FILES
                ) {
                    break;
                }

                String type =
                        node.path("type")
                                .asText("");

                String path =
                        node.path("path")
                                .asText("");

                if (
                        !"blob".equalsIgnoreCase(type) ||
                        path.isBlank()
                ) {

                    continue;
                }

                if (
                        isProblemDefinition(path)
                ) {

                    files.add(
                            normalizePath(path)
                    );
                }
            }

            return files;

        } catch (
                IOException |
                InterruptedException |
                IllegalArgumentException exception
        ) {

            if (
                    exception instanceof InterruptedException
            ) {

                Thread.currentThread()
                        .interrupt();
            }

            return Collections.emptyList();
        }
    }

    public String getFileContent(
            String repository,
            String path
    ) {

        String normalizedRepository =
                normalizeRepository(
                        repository
                );

        String normalizedPath =
                normalizePath(
                        path
                );

        if (
                !isValidRepository(
                        normalizedRepository
                ) ||
                normalizedPath.isBlank()
        ) {

            return "";
        }

        try {

            String branch =
                    getDefaultBranch(
                            normalizedRepository
                    );

            String rawUrl =
                    buildRawFileUrl(
                            normalizedRepository,
                            branch,
                            normalizedPath
                    );

            HttpResponse<String> rawResponse =
                    sendGet(rawUrl);

            if (
                    rawResponse.statusCode() == 200 &&
                    rawResponse.body() != null
            ) {

                return rawResponse.body();
            }

            return getFileContentFromApi(
                    normalizedRepository,
                    normalizedPath
            );

        } catch (
                IOException |
                InterruptedException |
                IllegalArgumentException exception
        ) {

            if (
                    exception instanceof InterruptedException
            ) {

                Thread.currentThread()
                        .interrupt();
            }

            return "";
        }
    }

    private String getFileContentFromApi(
            String repository,
            String path
    )
            throws IOException,
            InterruptedException {

        String encodedPath =
                encodePath(path);

        String url =
                "https://api.github.com/repos/"
                        + repository
                        + "/contents/"
                        + encodedPath;

        HttpResponse<String> response =
                sendGet(url);

        if (
                response.statusCode() != 200 ||
                response.body() == null ||
                response.body().isBlank()
        ) {

            return "";
        }

        JsonNode root =
                objectMapper.readTree(
                        response.body()
                );

        if (
                root == null ||
                !root.isObject()
        ) {

            return "";
        }

        String encoding =
                root.path("encoding")
                        .asText("");

        String content =
                root.path("content")
                        .asText("");

        if (content.isBlank()) {
            return "";
        }

        if (
                "base64".equalsIgnoreCase(
                        encoding
                )
        ) {

            try {

                byte[] decoded =
                        java.util.Base64
                                .getMimeDecoder()
                                .decode(content);

                return new String(
                        decoded,
                        StandardCharsets.UTF_8
                );

            } catch (
                    IllegalArgumentException exception
            ) {

                return "";
            }
        }

        return content;
    }

    private String getDefaultBranch(
            String repository
    )
            throws IOException,
            InterruptedException {

        String url =
                "https://api.github.com/repos/"
                        + repository;

        HttpResponse<String> response =
                sendGet(url);

        if (
                response.statusCode() != 200 ||
                response.body() == null ||
                response.body().isBlank()
        ) {

            return "main";
        }

        JsonNode root =
                objectMapper.readTree(
                        response.body()
                );

        String branch =
                root.path("default_branch")
                        .asText("");

        if (branch.isBlank()) {
            return "main";
        }

        return branch.trim();
    }

    private HttpResponse<String> sendGet(
            String url
    )
            throws IOException,
            InterruptedException {

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(url)
                        )
                        .timeout(
                                REQUEST_TIMEOUT
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
                HttpResponse.BodyHandlers
                        .ofString()
        );
    }

    private boolean isProblemDefinition(
            String path
    ) {

        String normalized =
                normalizePath(path)
                        .toLowerCase();

        if (
                normalized.startsWith(".git/") ||
                normalized.contains("/.git/")
        ) {

            return false;
        }

        if (
                normalized.startsWith("node_modules/") ||
                normalized.contains("/node_modules/")
        ) {

            return false;
        }

        if (
                normalized.startsWith("target/") ||
                normalized.contains("/target/")
        ) {

            return false;
        }

        if (
                normalized.startsWith("build/") ||
                normalized.contains("/build/")
        ) {

            return false;
        }

        if (
                normalized.startsWith("dist/") ||
                normalized.contains("/dist/")
        ) {

            return false;
        }

        if (
                normalized.startsWith(".idea/") ||
                normalized.contains("/.idea/")
        ) {

            return false;
        }

        return normalized.endsWith(
                "/problem.json"
        );
    }

    private String buildRawFileUrl(
            String repository,
            String branch,
            String path
    ) {

        return "https://raw.githubusercontent.com/"
                + repository
                + "/"
                + encodeBranch(branch)
                + "/"
                + encodePath(path);
    }

    private String encodeBranch(
            String branch
    ) {

        if (
                branch == null ||
                branch.isBlank()
        ) {

            return "main";
        }

        String normalizedBranch =
                branch.trim();

        String[] parts =
                normalizedBranch.split(
                        "/"
                );

        StringBuilder result =
                new StringBuilder();

        for (
                int index = 0;
                index < parts.length;
                index++
        ) {

            if (index > 0) {
                result.append("/");
            }

            result.append(
                    encodeSegment(
                            parts[index]
                    )
            );
        }

        return result.toString();
    }

    private String encodePath(
            String path
    ) {

        String normalizedPath =
                normalizePath(path);

        if (normalizedPath.isBlank()) {
            return "";
        }

        String[] parts =
                normalizedPath.split("/");

        StringBuilder result =
                new StringBuilder();

        for (
                int index = 0;
                index < parts.length;
                index++
        ) {

            if (index > 0) {
                result.append("/");
            }

            result.append(
                    encodeSegment(
                            parts[index]
                    )
            );
        }

        return result.toString();
    }

    private String encodeSegment(
            String value
    ) {

        return URLEncoder
                .encode(
                        value,
                        StandardCharsets.UTF_8
                )
                .replace(
                        "+",
                        "%20"
                );
    }

    private String normalizeRepository(
            String repository
    ) {

        if (
                repository == null ||
                repository.isBlank()
        ) {

            return "";
        }

        String normalized =
                repository.trim();

        normalized =
                normalized.replaceAll(
                        "^https?://(www\\.)?github\\.com/",
                        ""
                );

        normalized =
                normalized.replaceAll(
                        "/+$",
                        ""
                );

        normalized =
                normalized.replaceAll(
                        "\\.git$",
                        ""
                );

        normalized =
                normalized.replaceAll(
                        "/+$",
                        ""
                );

        String[] parts =
                normalized.split("/");

        if (
                parts.length < 2 ||
                parts[0].isBlank() ||
                parts[1].isBlank()
        ) {

            return "";
        }

        return parts[0].trim()
                + "/"
                + parts[1].trim();
    }

    private boolean isValidRepository(
            String repository
    ) {

        if (
                repository == null ||
                repository.isBlank()
        ) {

            return false;
        }

        String[] parts =
                repository.split("/");

        return parts.length == 2
                && !parts[0].isBlank()
                && !parts[1].isBlank();
    }

    private String normalizePath(
            String path
    ) {

        if (
                path == null ||
                path.isBlank()
        ) {

            return "";
        }

        return path
                .trim()
                .replace(
                        "\\",
                        "/"
                )
                .replaceAll(
                        "^/+",
                        ""
                )
                .replaceAll(
                        "/+$",
                        ""
                );
    }
}