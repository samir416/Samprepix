package com.aiinterview.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "github_connection",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_github_connection_user",
                        columnNames = "user_id"
                )
        }
)
public class GitHubConnection {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @OneToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private User user;

    @Column(
            name = "github_username",
            length = 100
    )
    private String githubUsername;

    @Column(
            name = "repository_url",
            length = 500
    )
    private String repositoryUrl;

    @Lob
    @Column(
            name = "access_token",
            nullable = true
    )
    private String accessToken;

    @Column(
            name = "connected_at",
            nullable = false
    )
    private LocalDateTime connectedAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    public GitHubConnection() {
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(
            User user
    ) {
        this.user = user;
    }

    public String getGithubUsername() {
        return githubUsername;
    }

    public void setGithubUsername(
            String githubUsername
    ) {
        this.githubUsername =
                githubUsername;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(
            String repositoryUrl
    ) {
        this.repositoryUrl =
                repositoryUrl;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(
            String accessToken
    ) {
        this.accessToken =
                accessToken;
    }

    public LocalDateTime getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(
            LocalDateTime connectedAt
    ) {
        this.connectedAt =
                connectedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(
            LocalDateTime updatedAt
    ) {
        this.updatedAt =
                updatedAt;
    }

    @PrePersist
    private void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        connectedAt = now;
        updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {

        updatedAt =
                LocalDateTime.now();
    }
}