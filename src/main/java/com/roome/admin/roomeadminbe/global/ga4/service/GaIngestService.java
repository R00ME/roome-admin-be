package com.roome.admin.roomeadminbe.global.ga4.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.analytics.data.v1beta.*;
import com.roome.admin.roomeadminbe.global.ga4.Ga4Properties;
import com.roome.admin.roomeadminbe.global.ga4.entity.FeatureEventDaily;
import com.roome.admin.roomeadminbe.global.ga4.repository.FeatureEventDailyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GaIngestService {
    // 요청 후보(전체)
    private static final List<String> DIM_CANDIDATES = List.of(
            "eventName",
            "customEvent:event_category", "customEvent:event_label",
            "customEvent:custom_parameter_1", "customEvent:custom_parameter_2", "customEvent:custom_parameter_3",
            "customEvent:feature_name", "customEvent:user_id", "customEvent:session_id", "customEvent:batch_id"
    );
    private static final List<String> MET_CANDIDATES = List.of(
            "eventCount",
            "customEvent:value", "customEvent:duration_ms", "customEvent:session_count",
            "customEvent:unique_features", "customEvent:unique_users", "customEvent:reward_points"
    );
    private final BetaAnalyticsDataClient ga;
    private final Ga4Properties properties;
    private final FeatureEventDailyRepository featureEventDailyRepository;

    private static String getDim(Row r, Map<String, Integer> dIdx, String apiName) {
        Integer i = dIdx.get(apiName);
        return (i == null) ? null : r.getDimensionValues(i).getValue();
    }

    private static long getMetLong(Row r, Map<String, Integer> mIdx, String apiName) {
        Integer i = mIdx.get(apiName);
        if (i == null) return 0L;
        String v = r.getMetricValues(i).getValue();
        return (v == null || v.isBlank()) ? 0L : Long.parseLong(v);
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

    @Transactional
    public void upsertDay(LocalDate day) {
        // 0) 메타데이터로 가용 차원/지표 조회 후 필터링
        var available = fetchAvailableFields();
        List<String> dims = DIM_CANDIDATES.stream()
                .filter(available.dimensions::contains)
                .toList();
        List<String> mets = MET_CANDIDATES.stream()
                .filter(available.metrics::contains)
                .toList();

        // 최소 보장(등록 전에도 동작하도록)
        if (dims.isEmpty()) dims = List.of("eventName");
        if (mets.isEmpty()) mets = List.of("eventCount");

        // 1) GA4에서 해당 일자 조회
        RunReportRequest.Builder b = RunReportRequest.newBuilder()
                .setProperty("properties/" + properties.getPropertyId())
                .addDateRanges(DateRange.newBuilder()
                        .setStartDate(day.toString()).setEndDate(day.toString()).build());
        dims.forEach(d -> b.addDimensions(Dimension.newBuilder().setName(d)));
        mets.forEach(m -> b.addMetrics(Metric.newBuilder().setName(m)));

        RunReportResponse resp = ga.runReport(b.build());

        // 2) 기존 데이터 삭제 후 저장
        featureEventDailyRepository.deleteByStatDate(day);

        List<FeatureEventDaily> rows = new ArrayList<>();
        for (Row r : resp.getRowsList()) rows.add(toEntity(day, r, dims, mets));
        featureEventDailyRepository.saveAll(rows);
    }

    // === 메타데이터 조회(가용 차원/지표 집합) ===
    private AvailableFields fetchAvailableFields() {
        String name = "properties/" + properties.getPropertyId() + "/metadata";
        Metadata md = ga.getMetadata(GetMetadataRequest.newBuilder().setName(name).build());

        var dimSet = md.getDimensionsList().stream()
                .map(DimensionMetadata::getApiName)   // 예: "eventName", "customEvent:feature_name"
                .collect(java.util.stream.Collectors.toSet());

        var metSet = md.getMetricsList().stream()
                .map(MetricMetadata::getApiName)      // 예: "eventCount", "customEvent:duration_ms"
                .collect(java.util.stream.Collectors.toSet());

        return new AvailableFields(dimSet, metSet);
    }

    // === Row 매핑 (인덱스가 동적이므로 이름→인덱스 맵 구성) ===
    private FeatureEventDaily toEntity(LocalDate day, Row r, List<String> dims, List<String> mets) {
        // dimension 값 꺼내기 위한 맵
        Map<String, Integer> dIdx = new LinkedHashMap<>();
        for (int i = 0; i < dims.size(); i++) dIdx.put(dims.get(i), i);
        Map<String, Integer> mIdx = new LinkedHashMap<>();
        for (int i = 0; i < mets.size(); i++) mIdx.put(mets.get(i), i);

        String eventName = getDim(r, dIdx, "eventName");
        String cat = getDim(r, dIdx, "customEvent:event_category");
        String label = getDim(r, dIdx, "customEvent:event_label");
        String cp1 = getDim(r, dIdx, "customEvent:custom_parameter_1");
        String cp2 = getDim(r, dIdx, "customEvent:custom_parameter_2");
        String cp3 = getDim(r, dIdx, "customEvent:custom_parameter_3");
        String featureName = getDim(r, dIdx, "customEvent:feature_name");
        String userId = getDim(r, dIdx, "customEvent:user_id");
        String sessionId = getDim(r, dIdx, "customEvent:session_id");
        String batchId = getDim(r, dIdx, "customEvent:batch_id");

        long eventCount = getMetLong(r, mIdx, "eventCount");
        Long value = getMetLongNull(r, mIdx, "customEvent:value");
        Long durationMs = getMetLongNull(r, mIdx, "customEvent:duration_ms");
        Long sessionCount = getMetLongNull(r, mIdx, "customEvent:session_count");
        Long uniqueFeat = getMetLongNull(r, mIdx, "customEvent:unique_features");
        Long uniqueUsers = getMetLongNull(r, mIdx, "customEvent:unique_users");
        Long rewardPoints = getMetLongNull(r, mIdx, "customEvent:reward_points");

        FeatureEventDaily d = new FeatureEventDaily();
        d.setStatDate(day);
        d.setEventName(eventName);
        d.setEventCategory(z(cat));
        d.setEventLabel(z(label));
        d.setFeatureName(z(featureName));
        d.setUserId(z(userId));
        d.setSessionId(z(sessionId));
        d.setBatchId(z(batchId));
        d.setEventCount(eventCount);
        d.setValueSum(value);
        d.setDurationMsSum(durationMs);
        d.setSessionCountSum(sessionCount);
        d.setUniqueFeaturesSum(uniqueFeat);
        d.setUniqueUsersSum(uniqueUsers);
        d.setRewardPointsSum(rewardPoints);

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("custom_parameter_1", z(cp1));
        params.put("custom_parameter_2", z(cp2));
        params.put("custom_parameter_3", z(cp3));
        d.setParamsJson(toJson(params));
        d.setCollectedAt(LocalDateTime.now());
        return d;
    }

    private record AvailableFields(java.util.Set<String> dimensions, java.util.Set<String> metrics) {
    }
}
