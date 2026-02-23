package com.roome.admin.roomeadminbe.domain.apiUsage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDomainStatsResponse {
    private Long userId;                        // 사용자 ID
    private PeriodDomainStatsResponse recent30Days;   // 오늘 ~ 30일 전
    private PeriodDomainStatsResponse recent90Days; // 최근 3달
}