package com.roome.admin.roomeadminbe.domain.ga4.service;

import com.roome.admin.roomeadminbe.domain.apiUsage.repository.UserApiUsageRepository;
import com.roome.admin.roomeadminbe.domain.common.entity.User;
import com.roome.admin.roomeadminbe.domain.common.repository.UserRepository;
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
import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GaService {

    private final GaUserPatternRepository gaUserPatternRepository;
    private final GaEventDailyRepository gaEventDailyRepository;
    private final GaFeatureStatRepository gaFeatureStatRepository;
    private final GaUserStatRepository gaUserStatRepository;
    private final UserRepository userRepository;
    private final UserApiUsageRepository userApiUsageRepository;

    public UserFeatureStatsResponse getUserFeatureUsage(String userId) {
        List<UserPatternResponse> results = gaUserPatternRepository.getUserFeatureUsage(userId);

        if (results.isEmpty()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 초 단위를 사람이 읽기 좋은 문자열로 변환
        results.forEach(response -> response.setUsageTime(formatDuration(response.getUsageTimeSec())));

        // 닉네임 가져오기 (예시: UserRepository or AdminRepository)
        String nickname = userRepository.findById(Long.parseLong(userId))
                .map(User::getNickname)
                .orElse("Unknown");

        return UserFeatureStatsResponse.builder()
                .userId(userId)
                .nickname(nickname)
                .featureStats(results)
                .build();
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

    public AiSummaryResponse getAiSummary() {
        AiSummaryResponse aiSummaryResponse = new AiSummaryResponse();
        aiSummaryResponse.setMostUsedFeature(gaFeatureStatRepository.getMostUsedFeature());
        aiSummaryResponse.setMostDroppedFeature(gaFeatureStatRepository.getMostDroppedFeature());
        aiSummaryResponse.setMostEntryPath(gaEventDailyRepository.getMostEntryPath());
        return aiSummaryResponse;
    }

    public UserActivityResponse getUserActivity(String userId) {
        List<ActivityTimeResponse> activityTime = gaEventDailyRepository.getUserActivityTime(userId);
        return UserActivityResponse.builder()
                .userId(userId)
                .activityTime(activityTime)
                .build();
    }

    public List<FeatureUsageResponse> getFeatureStats(Long userId) {
        // String 변환 (GaEventDaily 쿼리에 필요)
        String customUserId = String.valueOf(userId);

        // 각각 쿼리 실행
        List<FeatureUsageResponse> apiUsage = userApiUsageRepository.getApiUsageByUser(userId);
        List<FeatureUsageResponse> featureUsage = gaEventDailyRepository.getFeatureUsageByUser(customUserId);

        // feature 기준으로 merge
        Map<String, FeatureUsageResponse> merged = new HashMap<>();

        // apiUsage 추가
        apiUsage.forEach(stat -> merged.put(stat.getFeature(), stat));

        // featureUsage 병합
        featureUsage.forEach(feature -> {
            // usageTime 변환
            String usageTime = formatDuration(feature.getUsageTimeSec());
            feature.setUsageTime(usageTime);

            merged.merge(feature.getFeature(), feature, (api, fe) -> {
                // apiRequestCount 합산
                Long totalApiCount =
                        Optional.ofNullable(api.getApiRequestCount()).orElse(0L) +
                                Optional.ofNullable(fe.getApiRequestCount()).orElse(0L);

                // usageTimeSec (featureUsage 기준)
                Long usageTimeSec = Optional.ofNullable(fe.getUsageTimeSec()).orElse(0L);

                // 최근 사용일자 (둘 중 최신)
                LocalDate lastUsedAt = Stream.of(api.getLastUsedAt(), fe.getLastUsedAt())
                        .filter(Objects::nonNull)
                        .max(LocalDate::compareTo)
                        .orElse(null);

                // contentCount (현재 apiUsage 기준, 필요하면 합산 가능)
                Long contentCount = Optional.ofNullable(api.getContentCount()).orElse(0L);

                return FeatureUsageResponse.builder()
                        .feature(api.getFeature())
                        .apiRequestCount(totalApiCount)
                        .usageTime(formatDuration(usageTimeSec)) // 변환된 usageTime
                        .usageTimeSec(usageTimeSec)
                        .lastUsedAt(lastUsedAt)
                        .contentCount(contentCount)
                        .build();
            });
        });

        return new ArrayList<>(merged.values());
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
