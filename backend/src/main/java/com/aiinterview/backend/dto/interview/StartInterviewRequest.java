package com.aiinterview.backend.dto.interview;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartInterviewRequest {

    @NotBlank(message = "Interview type is required.")
    private String interviewType;

    @NotBlank(message = "Target role is required.")
    private String targetRole;

    @NotBlank(message = "Experience level is required.")
    private String experienceLevel;

    @NotEmpty(message = "At least one skill must be selected.")
    private List<String> skills;

}