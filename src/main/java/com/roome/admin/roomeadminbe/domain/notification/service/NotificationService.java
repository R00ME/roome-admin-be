package com.roome.admin.roomeadminbe.domain.notification.service;

import com.roome.admin.roomeadminbe.domain.notification.dto.NotificationRequestDto;
import com.roome.admin.roomeadminbe.domain.notification.dto.NotificationResponseDto;
import com.roome.admin.roomeadminbe.domain.notification.entity.Notification;
import com.roome.admin.roomeadminbe.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification createNotification(NotificationRequestDto dto) {
        Notification notification = Notification.builder()
                .notificationTitle(dto.getNotificationTitle())
                .notificationContent(dto.getNotificationContent())
                .category(dto.getCategory())
                .isUrgent(dto.isUrgent())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }

    public List<NotificationResponseDto> getAllNotifications() {
        return notificationRepository.findAll().stream()
                .map(NotificationResponseDto::new)
                .toList();
    }
}
