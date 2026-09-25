package com.aiinterview.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "github_analysis_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GithubAnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "github_username", nullable = false, length = 100)
    private String githubUsername;

    @Column(name = "profile_url", nullable = false, length = 255)
    private String profileUrl;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private int publicRepos;
    private int followers;
    private int following;
    private int overallScore;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String categoryScoresJson;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String deductionsJson;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String improvementsJson;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String topLanguagesJson;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String currentReadme;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String recommendedReadme;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String readmeDiffJson;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String repoAnalysesJson;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String recruiterView;

    @Column(nullable = false)
    private LocalDateTime analyzedAt;

    @PrePersist
    protected void onCreate() {
        if (analyzedAt == null) {
            analyzedAt = LocalDateTime.now();
        }
    }
}
