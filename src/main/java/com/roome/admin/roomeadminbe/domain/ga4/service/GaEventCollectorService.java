package com.roome.admin.roomeadminbe.domain.ga4.service;

import com.google.analytics.data.v1beta.*;
import com.roome.admin.roomeadminbe.domain.ga4.entity.GaEventDaily;
import com.roome.admin.roomeadminbe.domain.ga4.repository.FeatureStatRepository;
import com.roome.admin.roomeadminbe.domain.ga4.repository.GaEventDailyRepository;
import com.roome.admin.roomeadminbe.domain.ga4.repository.GaUserPatternRepository;
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
    private final FeatureStatRepository featureStatRepository;
    private final GaUserPatternRepository gaUserPatternRepository;

    @Value("${ga4.property-id}")
    private String propertyId;

    public void collectDailyEvents(LocalDate date) {
        RunReportRequest request = RunReportRequest.newBuilder()
                .setProperty("properties/" + propertyId)
                .addDateRanges(DateRange.newBuilder()
                        .setStartDate(date.toString())
                        .setEndDate(date.toString()))
                .addDimensions(Dimension.newBuilder().setName("eventName"))
//                .addDimensions(Dimension.newBuilder().setName("userId"))
                .addDimensions(Dimension.newBuilder().setName("customEvent:custom_user_id"))
                .addMetrics(Metric.newBuilder().setName("eventCount"))
                .addMetrics(Metric.newBuilder().setName("userEngagementDuration"))
                .build();

        RunReportResponse response = analyticsDataClient.runReport(request);

        for (Row row : response.getRowsList()) {
            GaEventDaily daily = GaEventDaily.builder()
                    .eventTime(date)
                    .eventName(row.getDimensionValues(0).getValue())
                    .userId(row.getDimensionValues(1).getValue())
//                    .sessionId(row.getDimensionValues(2).getValue())
                    .collectedAt(LocalDateTime.now()).build();
            dailyRepository.save(daily);

            // TODO: feature_stats, user_patterns → JSON 파싱 후 FeatureStat/UserPattern 엔티티로 분리 저장
        }
    }
}
