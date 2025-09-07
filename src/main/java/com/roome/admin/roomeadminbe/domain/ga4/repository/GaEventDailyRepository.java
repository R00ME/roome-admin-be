package com.roome.admin.roomeadminbe.domain.ga4.repository;

import com.roome.admin.roomeadminbe.domain.ga4.entity.GaEventDaily;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface GaEventDailyRepository extends JpaRepository<GaEventDaily, Long> {
    List<GaEventDaily> findByStatDate(LocalDate date);
}
