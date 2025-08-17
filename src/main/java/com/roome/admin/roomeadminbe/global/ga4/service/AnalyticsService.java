package com.roome.admin.roomeadminbe.global.ga4.service;

import com.roome.admin.roomeadminbe.global.ga4.dto.EventDailyDto;
import com.roome.admin.roomeadminbe.global.ga4.repository.FeatureEventDailyRepository;
import com.roome.admin.roomeadminbe.global.ga4.repository.FeatureEventDailyRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {
    private final FeatureEventDailyRepository featureEventDailyRepository;

    public Page<EventDailyDto> getDailyPage(LocalDate start, LocalDate end,
                                            String eventName, String eventCategory,
                                            String featureName, String userId,
                                            Pageable pageable) {
        return featureEventDailyRepository.searchDaily(start, end, eventName, eventCategory, featureName, userId, pageable);
    }

    public List<EventDailyDto> getDailySums(LocalDate start, LocalDate end,
                                            String eventName, String featureName) {
        return featureEventDailyRepository.sumByDay(start, end, eventName, featureName);
    }
}
