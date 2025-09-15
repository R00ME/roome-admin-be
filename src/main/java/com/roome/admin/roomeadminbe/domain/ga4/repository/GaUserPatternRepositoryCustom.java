package com.roome.admin.roomeadminbe.domain.ga4.repository;

import com.roome.admin.roomeadminbe.domain.ga4.dto.response.UserPatternResponse;

import java.util.List;

public interface GaUserPatternRepositoryCustom {
    List<UserPatternResponse> getUserFeatureUsage(String userId);
}
