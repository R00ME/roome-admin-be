package com.roome.admin.roomeadminbe.domain.notification.repository;

import com.roome.admin.roomeadminbe.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    //전체 읽음
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
    UPDATE bo_notifications n
    JOIN admin_notification an ON an.notification_id = n.notification_id
    SET n.is_read = 1
    WHERE an.admin_id = :adminId AND n.is_read = 0
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


    List<Notification> findByIsReadFalseOrderByCreatedAtDesc();

    List<Notification> findByIsUrgentTrueOrderByCreatedAtDesc();

    List<Notification> findAllByOrderByCreatedAtDesc();
    // 스키마 정리되면 다음과 같은 쿼리 메서드 추가 예정:
    // List<Notification> findAllByAdminEmail(String adminEmail);
    // 이후 소유자 검증 추가 시 예시:
    // Optional<Notification> findByNotificationIdAndAdmin_Email(Long id, String email);
}
