package com.roome.admin.roomeadminbe.domain.ga4.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GaUserPatternResponse {
    private String featureName;
    private Long eventCount;
    private Long usageTimeSec;
    private String usageTime;
}
