package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.GitHubConnection;
import com.aiinterview.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GitHubConnectionRepository
        extends JpaRepository<GitHubConnection, Long> {

    Optional<GitHubConnection> findByUser(
            User user
    );

    Optional<GitHubConnection> findByGithubUsername(
            String githubUsername
    );

    Optional<GitHubConnection> findByRepositoryUrl(
            String repositoryUrl
    );

    boolean existsByUser(
            User user
    );

    boolean existsByRepositoryUrl(
            String repositoryUrl
    );
}