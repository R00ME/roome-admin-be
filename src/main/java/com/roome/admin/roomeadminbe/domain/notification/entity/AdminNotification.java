package com.roome.admin.roomeadminbe.domain.notification.entity;

import com.roome.admin.roomeadminbe.domain.admin.entity.Admin;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admin_notification")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminNotification {

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

    // 생성자 (필요에 따라 추가 가능)
    public AdminNotification(Admin admin, Notification notification) {
        this.admin = admin;
        this.notification = notification;
    }
}

