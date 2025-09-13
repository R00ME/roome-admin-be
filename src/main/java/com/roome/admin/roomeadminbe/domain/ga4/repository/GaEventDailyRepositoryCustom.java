package com.roome.admin.roomeadminbe.domain.ga4.repository;

import com.roome.admin.roomeadminbe.domain.ga4.dto.response.ActivityTimeResponse;
import com.roome.admin.roomeadminbe.domain.ga4.dto.response.ChartResponse;
import com.roome.admin.roomeadminbe.domain.ga4.dto.response.FeatureUsageResponse;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GaEventDailyRepositoryCustom {

    String getInflowValue();

    String getInflowChangeRate();

    String getReferralValue();

    List<ChartResponse> getInflowChart();

    List<ChartResponse> getContentChart();

    List<ChartResponse> getReferralChart();

    String getMostEntryPath();

    List<ActivityTimeResponse> getUserActivityTime(String userId);

    List<FeatureUsageResponse> getFeatureUsageByUser(String customUserId);
}
