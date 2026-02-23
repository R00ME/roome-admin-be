package com.roome.admin.roomeadminbe.domain.ga4.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPatternResponse {
    private String featureName;
    private Long eventCount;
    private Long usageTimeSec;
    private String usageTime;
}
