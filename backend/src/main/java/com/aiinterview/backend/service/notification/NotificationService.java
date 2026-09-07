package com.aiinterview.backend.service.notification;

import com.aiinterview.backend.dto.notification.NotificationDto;
import com.aiinterview.backend.entity.User;

import java.util.List;

public interface NotificationService {
    List<NotificationDto> getUserNotifications(User user);
    int getUnreadCount(User user);
    void markAllAsRead(User user);
    void deleteNotification(User user, String notificationId);
    void clearAllNotifications(User user);
}

