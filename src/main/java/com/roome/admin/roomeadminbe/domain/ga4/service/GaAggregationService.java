package com.roome.admin.roomeadminbe.domain.ga4.service;

import com.roome.admin.roomeadminbe.domain.ga4.entity.GaEventDaily;
import com.roome.admin.roomeadminbe.domain.ga4.entity.GaFeatureStat;
import com.roome.admin.roomeadminbe.domain.ga4.entity.GaUserPattern;
import com.roome.admin.roomeadminbe.domain.ga4.repository.GaEventDailyRepository;
import com.roome.admin.roomeadminbe.domain.ga4.repository.GaFeatureStatRepository;
import com.roome.admin.roomeadminbe.domain.ga4.repository.GaUserPatternRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GaAggregationService {

    private final GaEventDailyRepository eventDailyRepo;
    private final GaFeatureStatRepository featureStatRepo;
    private final GaUserPatternRepository userPatternRepo;

    @Transactional
    public void aggregate(LocalDate date) {
        // 해당 날짜 데이터 조회
        List<GaEventDaily> events = eventDailyRepo.findByStatDate(date);

        // 기능별 통계 집계
        Map<String, List<GaEventDaily>> featureGroup =
                events.stream().collect(Collectors.groupingBy(GaEventDaily::getEventName));

        featureGroup.forEach((feature, list) -> {
            long totalUsers = list.stream()
                    .map(GaEventDaily::getCustomUserId).distinct().count();
            long totalDuration = list.stream()
                    .mapToLong(GaEventDaily::getDurationSec).sum();
            long eventCount = list.stream()
                    .mapToLong(GaEventDaily::getEventCount).sum();
            double avgDuration = list.isEmpty() ? 0 : (double) totalDuration / list.size();

            featureStatRepo.save(GaFeatureStat.builder()
                    .statDate(date)
                    .eventName(feature)
                    .totalUsers(totalUsers)
                    .totalDuration(totalDuration)
                    .eventCount(eventCount)
                    .avgDuration(avgDuration)
                    .build());
        });

        // 사용자별 패턴 집계
        Map<String, List<GaEventDaily>> userGroup =
                events.stream().collect(Collectors.groupingBy(e -> e.getCustomUserId() + "_" + e.getEventName()));

        userGroup.forEach((key, list) -> {
            String[] parts = key.split("_");
            String userId = parts[0];
            String feature = parts[1];

            long totalDuration = list.stream()
                    .mapToLong(GaEventDaily::getDurationSec).sum();
            long eventCount = list.stream()
                    .mapToLong(GaEventDaily::getEventCount).sum();
            LocalDateTime firstUseAt = list.stream()
                    .map(GaEventDaily::getCollectedAt).min(LocalDateTime::compareTo).orElse(null);
            LocalDateTime lastUseAt = list.stream()
                    .map(GaEventDaily::getCollectedAt).max(LocalDateTime::compareTo).orElse(null);

            userPatternRepo.save(GaUserPattern.builder()
                    .statDate(date)
                    .customUserId(userId)
                    .featureName(feature)
                    .totalDuration(totalDuration)
                    .eventCount(eventCount)
                    .firstUseAt(firstUseAt)
                    .lastUseAt(lastUseAt)
                    .build());
        });

        log.info("집계 완료: date={}, features={}, users={}",
                date, featureGroup.size(), userGroup.size());
    }
}
