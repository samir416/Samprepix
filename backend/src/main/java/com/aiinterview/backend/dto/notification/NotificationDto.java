package com.aiinterview.backend.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private String id;
    private String type; // "CODING", "INTERVIEW", "RESUME", "SYSTEM"
    private String title;
    private String message;
    private String timestamp;
    private LocalDateTime createdAt;
    private boolean unread;
    private String targetUrl;
}
