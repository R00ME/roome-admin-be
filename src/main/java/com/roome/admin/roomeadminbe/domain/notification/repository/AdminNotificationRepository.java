package com.roome.admin.roomeadminbe.domain.notification.repository;

import com.roome.admin.roomeadminbe.domain.notification.entity.Notification;
import com.roome.admin.roomeadminbe.domain.notification.entity.AdminNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdminNotificationRepository extends JpaRepository<AdminNotification, Long> {

    @Query("SELECT an.notification FROM AdminNotification an WHERE an.admin.adminId = :adminId")
    List<Notification> findNotificationsByAdminId(@Param("adminId") Long adminId);
}
