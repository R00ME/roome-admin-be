package com.roome.admin.roomeadminbe.domain.notification.dto;

import com.roome.admin.roomeadminbe.domain.notification.entity.AdminNotification;
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
        this.isRead = false;
        this.isUrgent = notification.isUrgent();
        this.createdAt = notification.getCreatedAt();
    }
    // 내 알림함(조인기반)에서는 AdminNotification 기준으로 생성
    public NotificationResponseDto(AdminNotification an) {
        Notification n = an.getNotification();
        this.notificationId = n.getNotificationId();
        this.notificationTitle = n.getNotificationTitle();
        this.notificationContent = n.getNotificationContent();
        this.category = n.getCategory();
        this.isUrgent = n.isUrgent();
        this.createdAt = n.getCreatedAt();
        this.isRead = an.isRead();
    }
}
