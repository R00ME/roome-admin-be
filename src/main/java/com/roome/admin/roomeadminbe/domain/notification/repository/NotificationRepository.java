package com.roome.admin.roomeadminbe.domain.notification.repository;

import com.roome.admin.roomeadminbe.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
