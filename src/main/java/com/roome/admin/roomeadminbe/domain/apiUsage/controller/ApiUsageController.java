package com.roome.admin.roomeadminbe.domain.apiUsage.controller;

import com.roome.admin.roomeadminbe.domain.apiUsage.dto.request.ApiUsageSearchRequest;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.request.UserMostUsedDomainSearchRequest;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.ApiUsageResponse;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.GetUserMostDomainResponse;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.UserDomainStatsResponse;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.UserPointTrendResponse;
import com.roome.admin.roomeadminbe.domain.apiUsage.service.ApiUsageService;
import com.roome.admin.roomeadminbe.domain.apiUsage.service.PointUsageService;
import com.roome.admin.roomeadminbe.domain.common.dto.response.CommonResponse;
import com.roome.admin.roomeadminbe.domain.common.dto.response.ListResponse;
import com.roome.admin.roomeadminbe.domain.ga4.dto.response.FeatureUsageResponse;
import com.roome.admin.roomeadminbe.domain.ga4.dto.response.UserActivityResponse;
import com.roome.admin.roomeadminbe.domain.ga4.dto.response.UserFeatureStatsResponse;
import com.roome.admin.roomeadminbe.domain.ga4.service.GaService;
import com.roome.admin.roomeadminbe.global.security.model.AdminDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.roome.admin.roomeadminbe.domain.common.dto.response.CommonResponse.ofDataWithHttpStatus;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin/usage")
public class ApiUsageController {

    private final ApiUsageService apiUsageService;
    private final GaService gaService;
    private final PointUsageService pointUsageService;

    @PreAuthorize("hasAnyRole('OPERATION_MANAGER', 'SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<CommonResponse<ListResponse<ApiUsageResponse>>> getApiUsageList(@AuthenticationPrincipal AdminDetails adminDetails, @ModelAttribute ApiUsageSearchRequest request) {
        return ofDataWithHttpStatus(apiUsageService.getApiUsageList(request), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('OPERATION_MANAGER', 'SUPER_ADMIN')")
    @GetMapping("/recent-user-activity")
    public ResponseEntity<CommonResponse<ListResponse<GetUserMostDomainResponse>>> getUsersMostUsedDomain(
            @ModelAttribute UserMostUsedDomainSearchRequest request) {

        return ofDataWithHttpStatus(apiUsageService.getUsersMostUsedDomain(request), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('OPERATION_MANAGER', 'SUPER_ADMIN')")
    @GetMapping("/{userId}/user-domain-stats")
    public ResponseEntity<CommonResponse<UserDomainStatsResponse>> getUserDomainStats(@AuthenticationPrincipal AdminDetails adminDetails, @PathVariable Long userId) {
        LocalDate startDate = LocalDate.now();
        UserDomainStatsResponse response = apiUsageService.getUserDomainStats(userId, startDate);
        return ofDataWithHttpStatus(response, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('OPERATION_MANAGER', 'SUPER_ADMIN')")
    @GetMapping("/{userId}/feature-stats")
    public ResponseEntity<CommonResponse<UserFeatureStatsResponse>> getUserFeatureUsage(
            @AuthenticationPrincipal AdminDetails adminDetails,
            @PathVariable String userId) {

        UserFeatureStatsResponse response = gaService.getUserFeatureUsage(userId);
        return CommonResponse.ofDataWithHttpStatus(response, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('OPERATION_MANAGER', 'SUPER_ADMIN')")
    @GetMapping("/{userId}/activity-time")
    public ResponseEntity<CommonResponse<UserActivityResponse>> getUserActivity(
            @PathVariable String userId) {

        UserActivityResponse response = gaService.getUserActivity(userId);
        return CommonResponse.ofDataWithHttpStatus(response, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('OPERATION_MANAGER', 'SUPER_ADMIN')")
    @GetMapping("/{userId}/points/trend")
    public ResponseEntity<CommonResponse<UserPointTrendResponse>> getUserPointTrend(
            @PathVariable Long userId) {

        UserPointTrendResponse response = pointUsageService.getUserPointTrend(userId);
        return CommonResponse.ofDataWithHttpStatus(response, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('OPERATION_MANAGER', 'SUPER_ADMIN')")
    @GetMapping("/{userId}/feature-stats/details")
    public ResponseEntity<CommonResponse<Map<String, Object>>> getFeatureStats(
            @PathVariable Long userId) {

        List<FeatureUsageResponse> featureStats = gaService.getFeatureStats(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("featureStats", featureStats);

        return CommonResponse.ofDataWithHttpStatus(response, HttpStatus.OK);
    }
}
