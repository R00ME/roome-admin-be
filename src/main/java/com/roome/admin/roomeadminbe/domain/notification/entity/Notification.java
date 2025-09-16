package com.roome.admin.roomeadminbe.domain.notification.entity;

import com.roome.admin.roomeadminbe.domain.common.entity.Timestamped;
import com.roome.admin.roomeadminbe.domain.notification.type.NotificationCategory;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "bo_notifications")
public class Notification extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "notification_title", nullable = false)
    private String notificationTitle;

    @Column(name = "notification_content", nullable = false)
    private String notificationContent;

//    @Column(name = "created_at", nullable = false)
//    private LocalDateTime createdAt;

//    @Column(name = "is_read", nullable = false)
//    private boolean isRead;

    @Column(name = "is_urgent", nullable = false)
    private boolean isUrgent;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private NotificationCategory category;

    @PrePersist //admin_notification에서 생성시간 동일하게
    void onCreate(){
        if (createdAt == null){
            createdAt = LocalDateTime.now();
        }
    }
}
