package com.roome.admin.roomeadminbe.domain.ga4.repository;

import com.roome.admin.roomeadminbe.domain.ga4.dto.EventDailyDto;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface FeatureEventDailyRepositoryCustom {
    Page<EventDailyDto> searchDaily(
            LocalDate start, LocalDate end,
            @Nullable String eventName,
            @Nullable String eventCategory,
            @Nullable String featureName,
            @Nullable String userId,
            Pageable pageable
    );

    // 예: 날짜별 집계(합계)만 반환
    List<EventDailyDto> sumByDay(
            LocalDate start, LocalDate end,
            @Nullable String eventName,
            @Nullable String featureName
    );
}
