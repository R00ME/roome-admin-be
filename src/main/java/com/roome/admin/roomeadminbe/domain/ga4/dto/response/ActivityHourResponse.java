package com.roome.admin.roomeadminbe.domain.ga4.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityHourResponse {
    private String timeRange;   // "오전 (6~12시)"
    private Long eventCount;    // 이벤트 수
    private Double percentage;  // 전체 대비 %
}
