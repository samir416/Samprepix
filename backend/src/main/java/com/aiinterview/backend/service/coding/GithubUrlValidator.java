package com.aiinterview.backend.service.coding;

import java.net.URI;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enterprise-grade security validator for GitHub URLs.
 * Strictly verifies HTTPS, github.com host, repository/profile path structure,
 * and defends against SSRF, arbitrary host requests, custom ports, and credentials.
 */
public final class GithubUrlValidator {

    private static final Pattern GITHUB_REPO_PATTERN = Pattern.compile(
            "^https://(?:www\\.)?github\\.com/([a-zA-Z0-9](?:[a-zA-Z0-9]|-(?=[a-zA-Z0-9])){0,38})/([a-zA-Z0-9_.-]{1,100}?)(?:\\.git)?/?$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern GITHUB_PROFILE_PATTERN = Pattern.compile(
            "^https://(?:www\\.)?github\\.com/([a-zA-Z0-9](?:[a-zA-Z0-9]|-(?=[a-zA-Z0-9])){0,38})/?$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Set<String> RESERVED_NAMES = Set.of(
            "about", "features", "pricing", "security", "login", "join", "enterprise",
            "explore", "marketplace", "sponsors", "settings", "notifications", "contact",
            "terms", "privacy", "organizations", "search", "trending", "stars", "api",
            "pulls", "issues", "new", "session", "dashboard", "site", "blog"
    );

    private GithubUrlValidator() {}

    /**
     * Validates that the URL is a safe HTTPS GitHub repository URL.
     * Throws IllegalArgumentException on any invalid syntax or security violation.
     */
    public static RepoInfo validateRepositoryUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("GitHub repository URL cannot be empty.");
        }
        String trimmed = url.trim();

        // Must start strictly with https://
        if (!trimmed.toLowerCase().startsWith("https://")) {
            throw new IllegalArgumentException("GitHub URL must use HTTPS protocol (https://github.com/owner/repository).");
        }

        // Forbid userinfo, query strings, fragments to prevent SSRF and filter bypasses
        if (trimmed.contains("@") || trimmed.contains("?") || trimmed.contains("#")) {
            throw new IllegalArgumentException("Invalid GitHub URL: query strings, fragments, and user credentials are not allowed.");
        }

        URI uri;
        try {
            uri = URI.create(trimmed);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL syntax.");
        }

        String host = uri.getHost();
        if (host == null || (!host.equalsIgnoreCase("github.com") && !host.equalsIgnoreCase("www.github.com"))) {
            throw new IllegalArgumentException("URL must belong strictly to the github.com domain.");
        }

        // Standard HTTPS port only (443 or default -1)
        if (uri.getPort() != -1 && uri.getPort() != 443) {
            throw new IllegalArgumentException("Custom ports are not allowed in GitHub URLs.");
        }

        Matcher matcher = GITHUB_REPO_PATTERN.matcher(trimmed);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid GitHub repository format. Expected: https://github.com/{owner}/{repository}");
        }

        String owner = matcher.group(1);
        String repo = matcher.group(2);

        if (RESERVED_NAMES.contains(owner.toLowerCase())) {
            throw new IllegalArgumentException("'" + owner + "' is a reserved GitHub keyword and cannot be used as a repository owner.");
        }
        if (repo.equals(".") || repo.equals("..") || repo.equalsIgnoreCase(".git")) {
            throw new IllegalArgumentException("Invalid repository name.");
        }

        String normalizedUrl = "https://github.com/" + owner + "/" + repo;
        return new RepoInfo(owner, repo, normalizedUrl);
    }

    /**
     * Validates that the URL is a safe HTTPS GitHub profile or repository URL.
     * Returns the normalized URL, or null if input was empty.
     */
    public static String validateProfileOrRepoUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        String trimmed = url.trim();

        if (!trimmed.toLowerCase().startsWith("https://")) {
            throw new IllegalArgumentException("GitHub URL must use HTTPS (https://github.com/username).");
        }
        if (trimmed.contains("@") || trimmed.contains("?") || trimmed.contains("#")) {
            throw new IllegalArgumentException("Invalid GitHub URL: query strings, fragments, and user credentials are not allowed.");
        }

        URI uri;
        try {
            uri = URI.create(trimmed);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL syntax.");
        }

        String host = uri.getHost();
        if (host == null || (!host.equalsIgnoreCase("github.com") && !host.equalsIgnoreCase("www.github.com"))) {
            throw new IllegalArgumentException("URL must belong strictly to github.com.");
        }

        if (uri.getPort() != -1 && uri.getPort() != 443) {
            throw new IllegalArgumentException("Custom ports are not allowed in GitHub URLs.");
        }

        Matcher repoMatcher = GITHUB_REPO_PATTERN.matcher(trimmed);
        if (repoMatcher.matches()) {
            String owner = repoMatcher.group(1);
            String repo = repoMatcher.group(2);
            if (RESERVED_NAMES.contains(owner.toLowerCase())) {
                throw new IllegalArgumentException("'" + owner + "' is a reserved GitHub keyword.");
            }
            return "https://github.com/" + owner + "/" + repo;
        }

        Matcher profileMatcher = GITHUB_PROFILE_PATTERN.matcher(trimmed);
        if (profileMatcher.matches()) {
            String username = profileMatcher.group(1);
            if (RESERVED_NAMES.contains(username.toLowerCase())) {
                throw new IllegalArgumentException("'" + username + "' is a reserved GitHub keyword.");
            }
            return "https://github.com/" + username;
        }

        throw new IllegalArgumentException("Please provide a valid GitHub profile or repository URL (e.g. https://github.com/username).");
    }

    public record RepoInfo(String owner, String repo, String normalizedUrl) {}
}
