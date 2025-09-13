package com.roome.admin.roomeadminbe.domain.ga4.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFeatureStatsResponse {
    private String userId;
    private String nickname;
    private List<UserPatternResponse> featureStats;
}
