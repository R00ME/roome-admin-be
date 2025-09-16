package com.roome.admin.roomeadminbe.domain.notification.entity;

import com.roome.admin.roomeadminbe.domain.admin.entity.Admin;
import com.roome.admin.roomeadminbe.domain.common.entity.Timestamped;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "admin_notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminNotification extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_type_id") // ERD에 맞게 수정
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead; // 기본 false

    @PrePersist
    void prePersist() {
        if (isRead == null) isRead = Boolean.FALSE;
    }// null 방지 → DB에 항상 0 저장

    // 서비스/기존 코드 호환용 편의 게터
    // 기존에 boolean일 때 쓰던 isRead()를 유지해줌
    public boolean isRead() {
        return Boolean.TRUE.equals(this.isRead);
    }

//    public void markRead()   { this.isRead = Boolean.TRUE; }
//    public void markUnread() { this.isRead = Boolean.FALSE; }

    // 생성자 (필요에 따라 추가 가능)
    @Builder
    public AdminNotification(Admin admin, Notification notification, Boolean isRead) {
        this.admin = admin;
        this.notification = notification;
        this.isRead = Boolean.TRUE.equals(isRead);
    }
}

