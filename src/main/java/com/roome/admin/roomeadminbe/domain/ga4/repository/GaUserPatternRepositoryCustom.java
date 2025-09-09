package com.roome.admin.roomeadminbe.domain.ga4.repository;

import com.roome.admin.roomeadminbe.domain.ga4.dto.response.GaUserPatternResponse;

import java.util.List;

public interface GaUserPatternRepositoryCustom {
    List<GaUserPatternResponse> getUserFeatureUsage(String userId);
}
