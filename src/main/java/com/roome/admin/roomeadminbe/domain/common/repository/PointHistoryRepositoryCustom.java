package com.roome.admin.roomeadminbe.domain.common.repository;

import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.PointTrendResponse;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointHistoryRepositoryCustom {
    List<PointTrendResponse> getUserPointTrend(Long userId);
}
