package com.roome.admin.roomeadminbe.domain.ga4.repository;

import com.roome.admin.roomeadminbe.domain.ga4.entity.FeatureEventDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Repository
public interface FeatureEventDailyRepository extends JpaRepository<FeatureEventDaily, Long>, FeatureEventDailyRepositoryCustom{
    @Modifying
    @Transactional
    int deleteByStatDate(LocalDate day);
}
