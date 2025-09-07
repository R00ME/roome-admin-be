package com.roome.admin.roomeadminbe.domain.ga4.service;

import com.google.analytics.data.v1beta.*;
import com.roome.admin.roomeadminbe.domain.ga4.entity.GaEventDaily;
import com.roome.admin.roomeadminbe.domain.ga4.repository.GaEventDailyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class GaEventCollectorService {

    private final BetaAnalyticsDataClient analyticsDataClient;
    private final GaEventDailyRepository dailyRepository;

    @Value("${ga4.property-id}")
    private String propertyId;
    @Transactional
    public void collectDailyEvents(LocalDate date) {
        RunReportRequest request = RunReportRequest.newBuilder()
                .setProperty("properties/" + propertyId)
                .addDateRanges(DateRange.newBuilder()
                        .setStartDate(date.toString())   // 예: 2025-09-06
                        .setEndDate(date.toString()))    // 하루 단위 집계
                .addDimensions(Dimension.newBuilder().setName("eventName"))
                .addDimensions(Dimension.newBuilder().setName("customEvent:custom_user_id"))
                .addMetrics(Metric.newBuilder().setName("eventCount"))
                .addMetrics(Metric.newBuilder().setName("customEvent:duration_sec"))
                .build();

        RunReportResponse response = analyticsDataClient.runReport(request);

        for (Row row : response.getRowsList()) {
            String eventName = row.getDimensionValues(0).getValue();
            String customUserId = row.getDimensionValues(1).getValue();
            Long eventCount = Long.parseLong(row.getMetricValues(0).getValue());
            Long durationSec = Long.parseLong(row.getMetricValues(1).getValue());

            GaEventDaily daily = GaEventDaily.builder()
                    .statDate(date)              // ← 전일자 기준으로 저장
                    .eventName(eventName)
                    .customUserId(customUserId)
                    .eventCount(eventCount)
                    .durationSec(durationSec)
                    .collectedAt(LocalDateTime.now()) // 실제 수집한 시각
                    .build();

            dailyRepository.save(daily);
        }

        log.info("GA 이벤트 데이터 저장 완료: {} rows (statDate={})", response.getRowsCount(), date);
    }
}
