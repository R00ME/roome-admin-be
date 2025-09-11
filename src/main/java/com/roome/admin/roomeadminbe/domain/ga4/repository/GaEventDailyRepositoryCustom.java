package com.roome.admin.roomeadminbe.domain.ga4.repository;

import com.roome.admin.roomeadminbe.domain.ga4.dto.response.ActivityHourResponse;
import com.roome.admin.roomeadminbe.domain.ga4.dto.response.ChartResponse;
import com.roome.admin.roomeadminbe.domain.ga4.dto.response.SummaryResponse;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GaEventDailyRepositoryCustom {

    List<ActivityHourResponse> getActivityByCustomRange(LocalDate date);
    String getMauValue();
    String getMauChangeRate();
    String getDauValue();
    String getDauChangeRate();
    String getInflowValue();
    String getInflowChangeRate();
    String getReferralValue();
    List<ChartResponse> getMauChart();
    List<ChartResponse> getDauChart();
    List<ChartResponse> getInflowChart();
    List<ChartResponse> getContentChart();
    List<ChartResponse> getReferralChart();
    String getMostEntryPath();
}
