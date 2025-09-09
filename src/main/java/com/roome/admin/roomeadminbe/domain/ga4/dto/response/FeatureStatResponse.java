package com.roome.admin.roomeadminbe.domain.ga4.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FeatureStatResponse {
    private String featureName;
    private Long apiRequestCount; // eventCount
    private String usageTime;     // "2H 30m" 형태
}
