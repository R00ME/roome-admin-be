package com.roome.admin.roomeadminbe.domain.apiUsage.service;

import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.PointTrendResponse;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.UserPointTrendResponse;
import com.roome.admin.roomeadminbe.domain.common.repository.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointUsageService {

    private final PointHistoryRepository pointHistoryRepository;

    @Transactional(readOnly = true)
    public UserPointTrendResponse getUserPointTrend(Long userId) {
        List<PointTrendResponse> trend = pointHistoryRepository.getUserPointTrend(userId);

        return UserPointTrendResponse.builder()
                .userId(userId)
                .trend(trend)
                .build();
    }
}
