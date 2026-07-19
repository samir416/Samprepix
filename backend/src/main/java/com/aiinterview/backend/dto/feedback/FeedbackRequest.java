package com.aiinterview.backend.dto.feedback;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for submitting interview feedback.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackRequest {

    /**
     * Interview Session ID
     */
    @NotNull(message = "Session ID is required.")
    private Long sessionId;

    /**
     * Rating given by the user (1 to 5).
     */
    @NotNull(message = "Rating is required.")
    @Min(value = 1, message = "Rating must be at least 1.")
    @Max(value = 5, message = "Rating cannot be greater than 5.")
    private Integer rating;

    /**
     * User's suggestion or feedback.
     */
    private String suggestion;

}