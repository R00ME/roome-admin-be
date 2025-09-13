package com.roome.admin.roomeadminbe.domain.ga4.dto.response;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeatureUsageResponse {
    private String feature;          // 기능명 (domain or featureName)
    private Long apiRequestCount;    // API 요청 수 (UserApiUsage)
    private String usageTime;        // 사용시간 (GaEventDaily)
    private Long usageTimeSec;
    private LocalDate lastUsedAt;    // 최근 사용일자
    private Long contentCount;       // 컨텐츠 작성 수 (옵션, GaEventDaily 기준)
}