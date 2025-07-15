package com.roome.admin.roomeadminbe.domain.alarm.repository;

import com.roome.admin.roomeadminbe.domain.alarm.entity.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
}
