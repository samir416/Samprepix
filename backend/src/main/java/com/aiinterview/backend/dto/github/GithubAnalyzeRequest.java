package com.aiinterview.backend.dto.github;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GithubAnalyzeRequest {

    @NotBlank(message = "GitHub Profile URL is required")
    private String profileUrl;
}
