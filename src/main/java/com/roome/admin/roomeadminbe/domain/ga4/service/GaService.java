package com.roome.admin.roomeadminbe.domain.ga4.service;

import com.roome.admin.roomeadminbe.domain.ga4.dto.response.*;
import com.roome.admin.roomeadminbe.domain.ga4.repository.GaEventDailyRepository;
import com.roome.admin.roomeadminbe.domain.ga4.repository.GaFeatureStatRepository;
import com.roome.admin.roomeadminbe.domain.ga4.repository.GaUserPatternRepository;
import com.roome.admin.roomeadminbe.domain.ga4.repository.GaUserStatRepository;
import com.roome.admin.roomeadminbe.global.exception.BusinessException;
import com.roome.admin.roomeadminbe.global.exception.enumeration.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GaService {

    private final GaUserPatternRepository gaUserPatternRepository;
    private final GaEventDailyRepository gaEventDailyRepository;
    private final GaFeatureStatRepository gaFeatureStatRepository;
    private final GaUserStatRepository gaUserStatRepository;

    public List<UserPatternResponse> getUserFeatureUsage(String userId) {
        List<UserPatternResponse> results = gaUserPatternRepository.getUserFeatureUsage(userId);

        if (results.isEmpty()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        // 초 단위를 사람이 읽기 좋은 문자열로 변환
        results.forEach(response -> response.setUsageTime(formatDuration(response.getUsageTimeSec())));

        return results;
    }

    public List<SummaryResponse> getSumaary() {

        SummaryResponse mau = new SummaryResponse("월간 활성 사용자수(MAU)"
                , gaUserStatRepository.getMauValue()
                , "명"
                , gaUserStatRepository.getMauChangeRate());

        SummaryResponse dau = new SummaryResponse("일간 활성 사용자수(DAU)"
                , gaUserStatRepository.getDauValue()
                , "명"
                , gaUserStatRepository.getDauChangeRate());

        SummaryResponse content = new SummaryResponse("콘텐츠 등록 수"
                , gaFeatureStatRepository.getContentValue()
                , "건"
                , gaFeatureStatRepository.getContentChangeRate());

        SummaryResponse inflow = new SummaryResponse("신규 사용자 수"
                , gaEventDailyRepository.getInflowValue()
                , "명"
                , gaEventDailyRepository.getInflowChangeRate());

        SummaryResponse referral = new SummaryResponse("유입경로"
                , gaEventDailyRepository.getReferralValue()
                , null
                , null);

        return List.of(mau, dau, content, inflow, referral);
    }

    public List<ChartResponse> getChart(String typeId) {
        if ("MAU".equals(typeId)) {
            return gaUserStatRepository.getMauChart();
        } else if ("DAU".equals(typeId)) {
            return gaUserStatRepository.getDauChart();
        } else if ("INFLOW".equals(typeId)) {
            return gaEventDailyRepository.getInflowChart();
        } else if ("CONTENT".equals(typeId)) {
            return gaEventDailyRepository.getContentChart();
        } else if ("REFERRAL".equals(typeId)) {
            return gaEventDailyRepository.getReferralChart();
        } else {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    public AiSummaryResponse getAiSummary(){
        AiSummaryResponse aiSummaryResponse = new AiSummaryResponse();
        aiSummaryResponse.setMostUsedFeature("가장 많이 사용한 기능은 "+gaFeatureStatRepository.getMostUsedFeature()+" 입니다.");
        aiSummaryResponse.setMostDroppedFeature("가장 이탈률이 많은 기능은 "+gaFeatureStatRepository.getMostDroppedFeature()+" 입니다.");
        aiSummaryResponse.setMostEntryPath(gaEventDailyRepository.getMostEntryPath()+" 경로에서 가장 많은 사용자가 유입되었습니다.");
        return aiSummaryResponse;
    }

    public UserActivityResponse getUserActivity(String userId) {
        List<ActivityTimeResponse> activityTime = gaEventDailyRepository.getUserActivityTime(userId);
        return UserActivityResponse.builder()
                .userId(userId)
                .activityTime(activityTime)
                .build();
    }

    private String formatDuration(Long seconds) {
        if (seconds == null || seconds < 0) return "0s";

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%dH %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }
}
