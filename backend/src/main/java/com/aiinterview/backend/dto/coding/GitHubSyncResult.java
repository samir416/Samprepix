package com.aiinterview.backend.dto.coding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitHubSyncResult {

    private boolean connected;

    private boolean synced;

    private boolean alreadySynced;

    private Integer solutionNumber;

    private String repositoryUrl;

    private String filePath;

    private String commitSha;

    private String commitUrl;

    private String fileUrl;

    private String message;

    private String error;
}
