//package com.roome.admin.roomeadminbe.domain.ga4.service;
//
//import com.roome.admin.roomeadminbe.domain.ga4.dto.response.ActivityHourResponse;
//import com.roome.admin.roomeadminbe.domain.ga4.dto.response.UserPatternResponse;
//import com.roome.admin.roomeadminbe.domain.ga4.repository.GaEventDailyRepository;
//import com.roome.admin.roomeadminbe.domain.ga4.repository.GaUserPatternRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class GaService {
//
//    private final GaUserPatternRepository gaUserPatternRepository;
//    private final GaEventDailyRepository gaEventDailyRepository;
//
//    public List<UserPatternResponse> getUserFeatureUsage(String userId) {
//        List<UserPatternResponse> results = gaUserPatternRepository.getUserFeatureUsage(userId);
//
//        // 초 단위를 사람이 읽기 좋은 문자열로 변환
//        results.forEach(response -> response.setUsageTime(formatDuration(response.getUsageTimeSec())));
//
//        return results;
//    }
//
//    public List<ActivityHourResponse> getActivitySummary(LocalDate date) {
//        return gaEventDailyRepository.getActivityByCustomRange(date);
//    }
//
//    private String formatDuration(Long seconds) {
//        if (seconds == null) return "0m";
//        long hours = seconds / 3600;
//        long minutes = (seconds % 3600) / 60;
//        return hours > 0 ? hours + "H " + minutes + "m" : minutes + "m";
//    }
//}
