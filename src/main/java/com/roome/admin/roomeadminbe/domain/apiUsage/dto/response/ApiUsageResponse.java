package com.roome.admin.roomeadminbe.domain.apiUsage.dto.response;

import com.roome.admin.roomeadminbe.domain.apiUsage.entity.UserApiUsage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ApiUsageResponse {
    private Long userId;
    private String domain;
    private String apiUri;
    private Long count;
    private LocalDate date; // 스케줄러에 의해 저장된 통계 날짜

    public static ApiUsageResponse from(UserApiUsage userApiUsage) {
        return new ApiUsageResponse(
                userApiUsage.getUserId(),
                userApiUsage.getDomain(),
                userApiUsage.getApiUri(),
                userApiUsage.getCount(),
                userApiUsage.getDate()
        );
    }
}
