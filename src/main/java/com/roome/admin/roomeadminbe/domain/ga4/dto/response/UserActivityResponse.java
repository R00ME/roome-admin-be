package com.roome.admin.roomeadminbe.domain.ga4.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserActivityResponse {
    private String userId;
    private List<ActivityTimeResponse> activityTime;
}
