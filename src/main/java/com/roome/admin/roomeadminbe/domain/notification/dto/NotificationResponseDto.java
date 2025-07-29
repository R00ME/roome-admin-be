package com.roome.admin.roomeadminbe.domain.notification.dto;

import com.roome.admin.roomeadminbe.domain.notification.entity.Notification;
import com.roome.admin.roomeadminbe.domain.notification.type.NotificationCategory;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotificationResponseDto {
    private final Long notificationId;
    private final NotificationCategory category;
    private final String notificationTitle;
    private final String notificationContent;
    private final Boolean isRead;
    private final Boolean isUrgent;
    private final LocalDateTime createdAt;

    public NotificationResponseDto(Notification notification) {
        this.notificationId = notification.getNotificationId();
        this.category = notification.getCategory();
        this.notificationTitle = notification.getNotificationTitle();
        this.notificationContent = notification.getNotificationContent();
        this.isRead = notification.isRead();
        this.isUrgent = notification.isUrgent();
        this.createdAt = notification.getCreatedAt();
    }
}
