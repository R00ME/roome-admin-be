package com.roome.admin.roomeadminbe.domain.apiUsage.dto.request;

import com.roome.admin.roomeadminbe.domain.common.dto.request.ListRequest;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ApiUsageSearchRequest extends ListRequest {
    private Long userId;          // 특정 사용자별 조회
    private String domain;        // 도메인별 조회
    private String apiUri;        // 특정 API 경로별 조회
    private LocalDate startDate;  // 기간 검색 (시작일)
    private LocalDate endDate;    // 기간 검색 (종료일)
}
