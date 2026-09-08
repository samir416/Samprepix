package com.aiinterview.backend.dto.aptitude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AptitudeSubmitRequest {
    private String trackId;
    private String trackTitle;
    private int timeSpentSeconds;
    private int timeLimitSeconds;
    private String completionReason; // "USER_SUBMITTED" or "TIMER_EXPIRED"
    private List<AnswerItem> answers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerItem {
        private Long questionId;
        private String questionCode;
        private String selectedOption; // "A", "B", "C", "D" or null
    }
}
