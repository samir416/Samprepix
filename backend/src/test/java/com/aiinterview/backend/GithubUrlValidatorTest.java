package com.aiinterview.backend;

import com.aiinterview.backend.service.coding.GithubUrlValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GithubUrlValidatorTest {

    @Test
    void testValidRepositoryUrls() {
        GithubUrlValidator.RepoInfo info = GithubUrlValidator.validateRepositoryUrl("https://github.com/octocat/Hello-World");
        assertEquals("octocat", info.owner());
        assertEquals("Hello-World", info.repo());
        assertEquals("https://github.com/octocat/Hello-World", info.normalizedUrl());

        GithubUrlValidator.RepoInfo gitInfo = GithubUrlValidator.validateRepositoryUrl("https://github.com/torvalds/linux.git");
        assertEquals("torvalds", gitInfo.owner());
        assertEquals("linux", gitInfo.repo());
        assertEquals("https://github.com/torvalds/linux", gitInfo.normalizedUrl());

        GithubUrlValidator.RepoInfo wwwInfo = GithubUrlValidator.validateRepositoryUrl("https://www.github.com/facebook/react/");
        assertEquals("facebook", wwwInfo.owner());
        assertEquals("react", wwwInfo.repo());
        assertEquals("https://github.com/facebook/react", wwwInfo.normalizedUrl());
    }

    @Test
    void testValidProfileUrls() {
        String profileUrl = GithubUrlValidator.validateProfileOrRepoUrl("https://github.com/octocat");
        assertEquals("https://github.com/octocat", profileUrl);

        String repoUrl = GithubUrlValidator.validateProfileOrRepoUrl("https://github.com/octocat/Spoon-Knife");
        assertEquals("https://github.com/octocat/Spoon-Knife", repoUrl);
    }

    @Test
    void testRejectInsecureHttp() {
        assertThrows(IllegalArgumentException.class, () ->
                GithubUrlValidator.validateRepositoryUrl("http://github.com/octocat/Hello-World"));

        assertThrows(IllegalArgumentException.class, () ->
                GithubUrlValidator.validateProfileOrRepoUrl("http://github.com/octocat"));
    }

    @Test
    void testRejectSsrfAndLocalhost() {
        assertThrows(IllegalArgumentException.class, () ->
                GithubUrlValidator.validateRepositoryUrl("http://localhost:8080/octocat/Hello-World"));

        assertThrows(IllegalArgumentException.class, () ->
                GithubUrlValidator.validateRepositoryUrl("http://127.0.0.1/octocat/Hello-World"));

        assertThrows(IllegalArgumentException.class, () ->
                GithubUrlValidator.validateRepositoryUrl("http://169.254.169.254/latest/meta-data"));

        assertThrows(IllegalArgumentException.class, () ->
                GithubUrlValidator.validateRepositoryUrl("https://10.0.0.1/octocat/Hello-World"));
    }

    @Test
    void testRejectArbitraryDomains() {
        assertThrows(IllegalArgumentException.class, () ->
                GithubUrlValidator.validateRepositoryUrl("https://evil.com/octocat/Hello-World"));

        assertThrows(IllegalArgumentException.class, () ->
                GithubUrlValidator.validateRepositoryUrl("https://github.com.evil.com/octocat/Hello-World"));
    }

    @Test
    void testRejectUserCredentialsAndQueryStrings() {
        assertThrows(IllegalArgumentException.class, () ->
                GithubUrlValidator.validateRepositoryUrl("https://admin:password@github.com/octocat/Hello-World"));

        assertThrows(IllegalArgumentException.class, () ->
                GithubUrlValidator.validateRepositoryUrl("https://github.com/octocat/Hello-World?key=val"));

        assertThrows(IllegalArgumentException.class, () ->
                GithubUrlValidator.validateRepositoryUrl("https://github.com/octocat/Hello-World#section"));
    }

    @Test
    void testRejectCustomPorts() {
        assertThrows(IllegalArgumentException.class, () ->
                GithubUrlValidator.validateRepositoryUrl("https://github.com:8443/octocat/Hello-World"));
    }

    @Test
    void testRejectReservedKeywords() {
        assertThrows(IllegalArgumentException.class, () ->
                GithubUrlValidator.validateRepositoryUrl("https://github.com/settings/my-repo"));

        assertThrows(IllegalArgumentException.class, () ->
                GithubUrlValidator.validateRepositoryUrl("https://github.com/security/my-repo"));
    }

    @Test
    void testRejectInsecureSchemes() {
        assertThrows(IllegalArgumentException.class, () ->
                GithubUrlValidator.validateRepositoryUrl("javascript:alert(1)"));

        assertThrows(IllegalArgumentException.class, () ->
                GithubUrlValidator.validateRepositoryUrl("file:///etc/passwd"));

        assertThrows(IllegalArgumentException.class, () ->
                GithubUrlValidator.validateRepositoryUrl("data:text/html,test"));
    }
}
