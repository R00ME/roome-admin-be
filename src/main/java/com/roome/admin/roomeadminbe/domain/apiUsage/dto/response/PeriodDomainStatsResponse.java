package com.roome.admin.roomeadminbe.domain.apiUsage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PeriodDomainStatsResponse {
    private Long totalCount;
    private List<DomainCountResponse> domainCounts; // 도메인별 카운트 리스트
}
