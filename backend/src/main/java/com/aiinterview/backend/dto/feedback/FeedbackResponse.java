package com.aiinterview.backend.dto.feedback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for interview feedback operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackResponse {

    /**
     * Indicates whether the request was successful.
     */
    private boolean success;

    /**
     * Response message.
     */
    private String message;

}