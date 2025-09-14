package com.roome.admin.roomeadminbe.domain.apiUsage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PointTrendResponse {

    private String month;        // ex) 2023-07
    private int earnedPoints;    // 해당 월 적립 합계
    private int usedPoints;      // 해당 월 사용 합계
}
