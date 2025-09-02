package com.roome.admin.roomeadminbe.domain.ga4.service;

import com.google.analytics.data.v1beta.*;
import com.roome.admin.roomeadminbe.domain.ga4.entity.GaEventDaily;
import com.roome.admin.roomeadminbe.domain.ga4.repository.GaEventDailyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GaEventCollectorService {

    private final BetaAnalyticsDataClient analyticsDataClient;
    private final GaEventDailyRepository dailyRepository;

    @Value("${ga.property-id}")
    private String propertyId;

    /**
     * 특정 날짜의 이벤트 데이터를 수집하여 Daily 집계 저장
     */
    public void collectDailyEvents(LocalDate date) {
        RunReportRequest request = RunReportRequest.newBuilder()
                .setProperty("properties/" + propertyId)
                .addDateRanges(DateRange.newBuilder()
                        .setStartDate(date.toString())
                        .setEndDate(date.toString()))
                .addDimensions(Dimension.newBuilder().setName("eventName"))
                .addDimensions(Dimension.newBuilder().setName("customEvent:feature_name"))
                .addMetrics(Metric.newBuilder().setName("eventCount"))
                .addMetrics(Metric.newBuilder().setName("customEvent:duration_ms"))
                .addMetrics(Metric.newBuilder().setName("customEvent:value"))
                .addMetrics(Metric.newBuilder().setName("customEvent:session_count"))
                .addMetrics(Metric.newBuilder().setName("customEvent:unique_users"))
                .build();

        RunReportResponse response = analyticsDataClient.runReport(request);

        // Daily 집계 저장
        for (Row row : response.getRowsList()) {
            String eventName = row.getDimensionValues(0).getValue();
            String featureName = row.getDimensionValues(1).getValue();

            Long eventCount = parseLongSafe(row.getMetricValues(0).getValue());
            Long durationMs = parseLongSafe(row.getMetricValues(1).getValue());
            Long value = parseLongSafe(row.getMetricValues(2).getValue());
            Long sessionCount = parseLongSafe(row.getMetricValues(3).getValue());
            Long uniqueUsers = parseLongSafe(row.getMetricValues(4).getValue());

            GaEventDaily daily = GaEventDaily.builder()
                    .statDate(date)
                    .eventName(eventName)
                    .featureName(featureName)
                    .eventCount(eventCount)
                    .durationMsSum(durationMs)
                    .rewardPointsSum(value) // 기존 value → rewardPointsSum 필드 매핑
                    .sessionCountSum(sessionCount)
                    .uniqueUsersSum(uniqueUsers)
                    .collectedAt(LocalDateTime.now())
                    .build();

            dailyRepository.save(daily);
        }
    }

    private Long parseLongSafe(String value) {
        return (value == null || value.isBlank()) ? 0L : Long.parseLong(value);
    }
}
