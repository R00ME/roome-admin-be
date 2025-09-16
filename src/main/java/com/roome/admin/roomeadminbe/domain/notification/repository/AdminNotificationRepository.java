package com.roome.admin.roomeadminbe.domain.notification.repository;

import com.roome.admin.roomeadminbe.domain.notification.entity.AdminNotification;
import com.roome.admin.roomeadminbe.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AdminNotificationRepository extends JpaRepository<AdminNotification, Long> {

    interface NotificationRow {
        Long getNotificationId();
        String getNotificationTitle();
        String getNotificationContent();
        String getCategory();
        Boolean getIsUrgent();
        Boolean getIsRead();
        java.sql.Timestamp getCreatedAt();       // 부모 created_at
        java.sql.Timestamp getModifiedAt();      // 부모 modified_at
        java.sql.Timestamp getAdminModifiedAt(); // 조인 modified_at
        java.sql.Timestamp getLastActivityAt();  // 정렬용(계산값)
    }

    @Query(value = """
        SELECT
          n.notification_id               AS notificationId,
          n.notification_title            AS notificationTitle,
          n.notification_content          AS notificationContent,
          n.category                      AS category,
          n.is_urgent                     AS isUrgent,
          an.is_read                      AS isRead,
          n.created_at                    AS createdAt,
          n.modified_at                   AS modifiedAt,
          an.modified_at                  AS adminModifiedAt,
          GREATEST(n.modified_at, an.modified_at) AS lastActivityAt
        FROM bo_notifications n
        JOIN admin_notification an
          ON an.notification_id = n.notification_id
        WHERE an.admin_id = :adminId
        ORDER BY lastActivityAt DESC, n.notification_id DESC
        """, nativeQuery = true)
    List<NotificationRow> findForAdminOrderByLastActivity(@Param("adminId") Long adminId);

    // 1) 읽음 처리 (업데이트 쿼리)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AdminNotification an
           SET an.isRead = TRUE
         WHERE an.admin.adminId = :adminId
           AND an.notification.notificationId = :notificationId
    """)
    int markRead(@Param("adminId") Long adminId,
                 @Param("notificationId") Long notificationId);

    // 내 알림함: 최신순(생성일 DESC, id DESC)
    List<AdminNotification> findByAdmin_AdminIdOrderByNotification_CreatedAtDescNotification_NotificationIdDesc(Long adminId);


    // [B] 전체 읽음 처리(내 것 전부)
    @Modifying
    @Transactional
    @Query("""
        update AdminNotification an
           set an.isRead = true
         where an.admin.adminId = :adminId
           and (an.isRead = false or an.isRead is null)
    """)
    int markAllRead(@Param("adminId") Long adminId);

    // [C] 내 알림(Notification) 목록 (createdAt desc, id desc)
    @Query("""
        select n
          from AdminNotification an
          join an.notification n
         where an.admin.adminId = :adminId
         order by n.createdAt desc, n.notificationId desc
    """)


    List<Notification> findNotificationsByAdminIdOrderByCreatedAtDesc(@Param("adminId") Long adminId);
}