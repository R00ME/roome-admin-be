package com.roome.admin.roomeadminbe.domain.notification.repository;

import com.roome.admin.roomeadminbe.domain.notification.entity.AdminNotification;
import com.roome.admin.roomeadminbe.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdminNotificationRepository extends JpaRepository<AdminNotification, Long> {

    @Query("""
            SELECT n
            FROM AdminNotification an
            JOIN an.notification n
            WHERE an.admin.adminId = :adminId
            ORDER by n.createdAt desc, n.notificationId desc
            """)
    List<Notification> findNotificationsByAdminIdOrderByCreatedAtDesc(@Param("adminId") Long adminId);
}
