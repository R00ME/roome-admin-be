package com.roome.admin.roomeadminbe.domain.ga4.repository;

import com.roome.admin.roomeadminbe.domain.ga4.dto.response.ChartResponse;

import java.util.List;

public interface GaUserStatRepositoryCustom {
    String getMauValue();
    String getMauChangeRate();
    String getDauValue();
    String getDauChangeRate();
    List<ChartResponse> getMauChart();
    List<ChartResponse> getDauChart();
}
