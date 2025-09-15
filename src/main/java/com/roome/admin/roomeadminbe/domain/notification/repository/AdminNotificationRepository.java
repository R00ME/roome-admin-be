package com.roome.admin.roomeadminbe.domain.notification.repository;

import com.roome.admin.roomeadminbe.domain.notification.entity.Notification;
import com.roome.admin.roomeadminbe.domain.notification.entity.AdminNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AdminNotificationRepository extends JpaRepository<AdminNotification, Long> {

    // 특정 관리자(adminId)의 모든 알림(조인) 최신순
    List<AdminNotification> findByAdmin_AdminIdOrderByNotification_CreatedAtDesc(Long adminId);

    // 특정 관리자(adminId)의 읽지 않은 개수
    long countByAdmin_AdminIdAndIsReadFalse(Long adminId);

    // 특정 관리자(adminId)의 긴급 + 미읽음 개수
    long countByAdmin_AdminIdAndIsReadFalseAndNotification_IsUrgentTrue(Long adminId);

    // 단건 조회: 관리자 소유 알림인지 검증
    Optional<AdminNotification> findByAdmin_AdminIdAndNotification_NotificationId(Long adminId, Long notificationId);

    //특정 관리자 "안읽음" 목록 최신순
    List<AdminNotification> findByAdmin_AdminIdAndIsReadFalseOrderByNotification_CreatedAtDesc(Long adminId);

    //특정 관리자 "긴급" 목록 최신순
    List<AdminNotification> findByAdmin_AdminIdAndNotification_IsUrgentTrueOrderByNotification_CreatedAtDesc(Long adminId);


    //전체 읽음(긴급 포함)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AdminNotification a
        SET a.isRead = true
        WHERE a.admin.adminId = :adminId AND a.isRead = false
    """)
    int markAllAsReadByAdminId(@Param("adminId") Long adminId);

}
