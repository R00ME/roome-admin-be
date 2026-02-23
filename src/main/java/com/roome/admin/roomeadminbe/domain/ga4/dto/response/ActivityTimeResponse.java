package com.roome.admin.roomeadminbe.domain.ga4.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ActivityTimeResponse {

    private String timeRange;
    private long count;       // 이벤트 수
    private double ratio;
}
