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
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Optional;

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
                        .setStartDate(date.toString())
                        .setEndDate(date.toString()))
                .addDimensions(Dimension.newBuilder().setName("eventName"))
                .addDimensions(Dimension.newBuilder().setName("customEvent:custom_user_id"))
                .addDimensions(Dimension.newBuilder().setName("customEvent:timestamp")) // 프론트에서 보낸 timestamp
                .addMetrics(Metric.newBuilder().setName("eventCount"))
                .addMetrics(Metric.newBuilder().setName("customEvent:duration_sec"))
                .build();

        RunReportResponse response = analyticsDataClient.runReport(request);

        for (Row row : response.getRowsList()) {
            try {
                String eventName = row.getDimensionValues(0).getValue();
                String customUserId = row.getDimensionValues(1).getValue();
                String timestampStr = row.getDimensionValues(2).getValue();
                LocalDateTime eventAt = null;
                String featureName = null;
                if (eventName.contains("usage")) {
                    String[] eventNameArray = eventName.split("_");
                    featureName = eventNameArray[0];
                }

                // "(not set)"이나 빈 문자열은 무시
                if (timestampStr != null && !timestampStr.isBlank() && !"(not set)".equals(timestampStr)) {
                    try {
                        eventAt = OffsetDateTime.parse(timestampStr)
                                .atZoneSameInstant(ZoneId.of("Asia/Seoul"))
                                .toLocalDateTime();
                    } catch (DateTimeParseException e) {
                        log.warn("timestamp 파싱 실패: {} (row={})", timestampStr, row, e);
                    }
                }

                Long eventCount = parseLongSafe(row.getMetricValues(0).getValue());
                Long durationSec = parseLongSafe(row.getMetricValues(1).getValue());

                Optional<GaEventDaily> existing = dailyRepository
                        .findByStatDateAndEventNameAndCustomUserId(date, eventName, customUserId);

                if (existing.isPresent()) {
                    existing.get().update(eventCount, durationSec, eventAt, LocalDateTime.now());
                } else {
                    GaEventDaily daily = GaEventDaily.builder()
                            .statDate(date)
                            .eventName(eventName)
                            .featureName(featureName)
                            .customUserId(customUserId)
                            .eventCount(eventCount)
                            .durationSec(durationSec)
                            .eventAt(eventAt)
                            .collectedAt(LocalDateTime.now())
                            .build();
                    dailyRepository.save(daily);
                }

                log.info("Saved Event - eventName={}, userId={}, count={}, durationSec={}",
                        eventName, customUserId, eventCount, durationSec);

            } catch (Exception e) {
                log.warn("Row 처리 실패: {}", row, e);
            }
        }

        log.info("GA 이벤트 데이터 저장 완료: {} rows (statDate={})", response.getRowsCount(), date);
    }

    private Long parseLongSafe(String value) {
        try {
            return (value != null && !value.isBlank()) ? Long.parseLong(value) : 0L;
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
