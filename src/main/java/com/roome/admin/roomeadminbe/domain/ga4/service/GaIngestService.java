package com.roome.admin.roomeadminbe.domain.ga4.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.analytics.data.v1beta.*;
import com.roome.admin.roomeadminbe.domain.ga4.Ga4Properties;
import com.roome.admin.roomeadminbe.domain.ga4.entity.GaEventRaw;
import com.roome.admin.roomeadminbe.domain.ga4.repository.GaEventRawRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
public class GaIngestService {

    private static final List<String> DIM_CANDIDATES = List.of(
            "eventName",
            "customEvent:feature_name",
            "customEvent:user_id",
            "customEvent:session_id",
            "customEvent:batch_id",
            "customEvent:custom_parameter_1",
            "customEvent:custom_parameter_2",
            "customEvent:custom_parameter_3"
    );

    private static final List<String> MET_CANDIDATES = List.of(
            "eventCount",
            "customEvent:value",
            "customEvent:duration_ms",
            "customEvent:reward_points"
    );

    private final BetaAnalyticsDataClient ga;
    private final Ga4Properties properties;
    private final GaEventRawRepository rawRepository;

    // === Raw 이벤트 수집 ===
    @Transactional
    public void ingestRawEvents(LocalDate day) {
        // 사용 가능한 dimension/metric 확인
        AvailableFields available = fetchAvailableFields();

        List<String> dims = DIM_CANDIDATES.stream()
                .filter(available.dimensions()::contains)
                .toList();
        List<String> mets = MET_CANDIDATES.stream()
                .filter(available.metrics()::contains)
                .toList();

        // 최소 fallback
        if (dims.isEmpty()) {
            dims = List.of("eventName");
        }
        if (mets.isEmpty()) {
            mets = List.of("eventCount");
        }

        // GA4 요청
        RunReportRequest.Builder builder = RunReportRequest.newBuilder()
                .setProperty("properties/" + properties.propertyId())
                .addDateRanges(DateRange.newBuilder()
                        .setStartDate(day.toString())
                        .setEndDate(day.toString())
                        .build());

        for (String d : dims) {
            builder.addDimensions(Dimension.newBuilder().setName(d));
        }
        for (String m : mets) {
            builder.addMetrics(Metric.newBuilder().setName(m));
        }

        RunReportResponse response = ga.runReport(builder.build());

        // 결과 저장
        List<GaEventRaw> rows = new ArrayList<>();
        for (Row r : response.getRowsList()) {
            rows.add(toEntity(r, dims, mets));
        }

        rawRepository.saveAll(rows);
    }

    // === Row → Raw 엔티티 변환 ===
    private GaEventRaw toEntity(Row r, List<String> dims, List<String> mets) {
        Map<String, Integer> dIdx = new LinkedHashMap<>();
        for (int i = 0; i < dims.size(); i++) {
            dIdx.put(dims.get(i), i);
        }

        Map<String, Integer> mIdx = new LinkedHashMap<>();
        for (int i = 0; i < mets.size(); i++) {
            mIdx.put(mets.get(i), i);
        }

        String eventName = getDim(r, dIdx, "eventName");
        String featureName = getDim(r, dIdx, "customEvent:feature_name");
        String userId = getDim(r, dIdx, "customEvent:user_id");
        String sessionId = getDim(r, dIdx, "customEvent:session_id");
        String batchId = getDim(r, dIdx, "customEvent:batch_id");

        Long durationMs = getMetLongNull(r, mIdx, "customEvent:duration_ms");
        Long value = getMetLongNull(r, mIdx, "customEvent:value");
        Long rewardPoints = getMetLongNull(r, mIdx, "customEvent:reward_points");

        // 커스텀 파라미터 JSON 저장
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("custom_parameter_1", getDim(r, dIdx, "customEvent:custom_parameter_1"));
        params.put("custom_parameter_2", getDim(r, dIdx, "customEvent:custom_parameter_2"));
        params.put("custom_parameter_3", getDim(r, dIdx, "customEvent:custom_parameter_3"));

        return GaEventRaw.builder()
                .eventName(eventName)
                .featureName(z(featureName))
                .userId(z(userId))
                .sessionId(z(sessionId))
                .batchId(z(batchId))
                .durationMs(durationMs)
                .value(value)
                .rewardPoints(rewardPoints)
                .paramsJson(toJson(params))
                .collectedAt(LocalDateTime.now())
                .build();
    }

    // === 헬퍼 ===
    private static String getDim(Row r, Map<String, Integer> dIdx, String apiName) {
        Integer i = dIdx.get(apiName);
        return (i == null) ? null : r.getDimensionValues(i).getValue();
    }

    private static Long getMetLongNull(Row r, Map<String, Integer> mIdx, String apiName) {
        Integer i = mIdx.get(apiName);
        if (i == null) return null;
        String v = r.getMetricValues(i).getValue();
        return (v == null || v.isBlank()) ? null : Long.parseLong(v);
    }

    private static String z(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private static String toJson(Map<String, Object> m) {
        try {
            return new ObjectMapper().writeValueAsString(m);
        } catch (Exception e) {
            return "{}";
        }
    }

    private record AvailableFields(Set<String> dimensions, Set<String> metrics) {
    }

    private AvailableFields fetchAvailableFields() {
        String name = "properties/" + properties.propertyId() + "/metadata";
        Metadata md = ga.getMetadata(GetMetadataRequest.newBuilder().setName(name).build());

        Set<String> dimSet = md.getDimensionsList().stream()
                .map(DimensionMetadata::getApiName)
                .collect(java.util.stream.Collectors.toSet());

        Set<String> metSet = md.getMetricsList().stream()
                .map(MetricMetadata::getApiName)
                .collect(java.util.stream.Collectors.toSet());

        return new AvailableFields(dimSet, metSet);
    }
}