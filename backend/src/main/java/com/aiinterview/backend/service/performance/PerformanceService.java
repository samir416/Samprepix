package com.aiinterview.backend.service.performance;

import com.aiinterview.backend.dto.performance.PerformanceAnalyticsDto;
import com.aiinterview.backend.entity.User;

public interface PerformanceService {
    PerformanceAnalyticsDto getPerformanceAnalytics(User user);
}
