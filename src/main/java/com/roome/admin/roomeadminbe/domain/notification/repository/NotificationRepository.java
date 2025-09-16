package com.roome.admin.roomeadminbe.domain.notification.repository;

import com.roome.admin.roomeadminbe.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE bo_notifications n
            JOIN admin_notification an ON an.notification_id = n.notification_id
            SET n.is_read = 1
            WHERE an.admin_id = :adminId
            """, nativeQuery = true)
    int markAllAsReadByAdminId(@Param("adminId") Long adminId);

    // 배지 갱신 등에서 사용할 미읽음 카운트
    @Query(value = """
            SELECT COUNT(*)
            FROM bo_notifications n
            JOIN admin_notification an ON an.notification_id = n.notification_id
            WHERE an.admin_id = :adminId AND n.is_read = 0
            """, nativeQuery = true)
    long countUnreadByAdminId(@Param("adminId") Long adminId);

    List<Notification> findAllByOrderByCreatedAtDesc();
}
