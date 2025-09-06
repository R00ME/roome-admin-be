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

    /**
     * 특정 날짜에 대한 이벤트 데이터를 GA API에서 가져와 DB에 저장
     */
    @Transactional
    public void collectDailyEvents(LocalDate date) {
        RunReportRequest request = RunReportRequest.newBuilder()
                .setProperty("properties/" + propertyId)
                .addDateRanges(DateRange.newBuilder()
                        .setStartDate(date.toString())
                        .setEndDate(date.toString()))
                // 이벤트 이름
                .addDimensions(Dimension.newBuilder().setName("eventName"))
                // 프론트에서 보낸 custom_user_id
                .addDimensions(Dimension.newBuilder().setName("customEvent:custom_user_id"))
                .addDimensions(Dimension.newBuilder().setName("customEvent:feature_name"))
                .addMetrics(Metric.newBuilder().setName("eventCount"))
                .addMetrics(Metric.newBuilder().setName("userEngagementDuration"))
                .build();

        RunReportResponse response = analyticsDataClient.runReport(request);

        for (Row row : response.getRowsList()) {
            String eventName = row.getDimensionValues(0).getValue();
            String customUserId = row.getDimensionValues(1).getValue();
            String featureName = row.getDimensionValues(2).getValue();
            Long eventCount = Long.parseLong(row.getMetricValues(0).getValue());
            Long engagementDuration = Long.parseLong(row.getMetricValues(1).getValue());

            GaEventDaily daily = GaEventDaily.builder()
                    .statDate(date)
                    .eventName(eventName)
                    .customUserId(customUserId)
                    .eventCount(eventCount)
                    .featureName(featureName)
                    .engagementDuration(engagementDuration)
                    .collectedAt(LocalDateTime.now())
                    .build();

            dailyRepository.save(daily);
        }

        log.info("GA 이벤트 데이터 저장 완료: {} rows", response.getRowsCount());
    }
}
