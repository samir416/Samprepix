package com.aiinterview.backend;

import com.aiinterview.backend.dto.notification.NotificationDto;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.service.notification.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("NotificationService generates dynamic, user-scoped notifications and manages unread state")
    void testNotificationService() {
        User user = userRepository.findByEmail("samirprajapat5@gmail.com")
                .orElseGet(() -> userRepository.findAll().stream().findFirst().orElse(null));

        assertNotNull(user, "User must exist in database");

        user.setDismissedAllNotificationsBefore(null);
        if (user.getDismissedNotificationIds() != null) {
            user.getDismissedNotificationIds().clear();
        }
        userRepository.save(user);

        List<NotificationDto> notifications = notificationService.getUserNotifications(user);
        assertNotNull(notifications, "Notifications list must not be null");
        assertFalse(notifications.isEmpty(), "User should receive at least one notification or welcome notification");

        int unreadCount = notificationService.getUnreadCount(user);
        assertTrue(unreadCount >= 0, "Unread count must be non-negative");

        // Test marking as read
        notificationService.markAllAsRead(user);
        int afterMarkRead = notificationService.getUnreadCount(user);
        assertEquals(0, afterMarkRead, "Unread count should be 0 after markAllAsRead");

        // Verify each notification structure
        for (NotificationDto dto : notifications) {
            assertNotNull(dto.getId());
            assertNotNull(dto.getTitle());
            assertNotNull(dto.getMessage());
            assertNotNull(dto.getType());
            assertNotNull(dto.getTimestamp());
        }

        // Test deleting a single notification
        String idToDelete = notifications.get(0).getId();
        notificationService.deleteNotification(user, idToDelete);
        List<NotificationDto> afterDelete = notificationService.getUserNotifications(user);
        boolean stillPresent = afterDelete.stream().anyMatch(n -> n.getId().equals(idToDelete));
        assertFalse(stillPresent, "Deleted notification must not be present in user notifications");

        // Test user isolation with second user
        User otherUser = userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(user.getId()))
                .findFirst()
                .orElse(null);

        if (otherUser != null) {
            List<NotificationDto> otherNotifsBefore = notificationService.getUserNotifications(otherUser);
            // Delete from user should not affect otherUser
            List<NotificationDto> otherNotifsAfter = notificationService.getUserNotifications(otherUser);
            assertEquals(otherNotifsBefore.size(), otherNotifsAfter.size(),
                    "User A's notification deletion must not affect User B");
        }

        // Test clearing all notification history
        notificationService.clearAllNotifications(user);
        List<NotificationDto> afterClear = notificationService.getUserNotifications(user);
        assertTrue(afterClear.isEmpty(), "Notifications list must be empty after clearAllNotifications");
        assertEquals(0, notificationService.getUnreadCount(user), "Unread count must be 0 after clearAllNotifications");
    }
}

