package com.aiinterview.backend.dto.admin;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminStatsResponse {
    private long totalUsers;
    private long activeUsers;
    private long pendingUsers;
    private long totalSubscriptions;
    private long activeSubscriptions;
    private long totalPayments;
    private double totalRevenue;
    private Map<String, Long> planDistribution;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
}
