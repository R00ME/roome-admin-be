package com.roome.admin.roomeadminbe.domain.apiUsage.controller;

import com.roome.admin.roomeadminbe.domain.apiUsage.dto.request.ApiUsageSearchRequest;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.request.UserMostUsedDomainSearchRequest;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.ApiUsageResponse;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.GetUserMostDomainResponse;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.UserDomainStatsResponse;
import com.roome.admin.roomeadminbe.domain.apiUsage.service.ApiUsageService;
import com.roome.admin.roomeadminbe.domain.common.dto.response.CommonResponse;
import com.roome.admin.roomeadminbe.domain.common.dto.response.ListResponse;
import com.roome.admin.roomeadminbe.global.security.model.AdminDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static com.roome.admin.roomeadminbe.domain.common.dto.response.CommonResponse.ofDataWithHttpStatus;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin/usage")
public class ApiUsageController {

    private final ApiUsageService apiUsageService;
//    private final UserStatisticsTupleService userStatisticsTupleService;

    @PreAuthorize("hasRole('OPERATION_MANAGER')")
    @GetMapping
    public ResponseEntity<CommonResponse<ListResponse<ApiUsageResponse>>> getApiUsageList(@AuthenticationPrincipal AdminDetails adminDetails, @ModelAttribute ApiUsageSearchRequest request) {
        return ofDataWithHttpStatus(apiUsageService.getApiUsageList(request), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('OPERATION_MANAGER')")
    @GetMapping("/recent-user-activity")
    public ResponseEntity<CommonResponse<ListResponse<GetUserMostDomainResponse>>> getUsersMostUsedDomain(
            @ModelAttribute UserMostUsedDomainSearchRequest request) {

        return ofDataWithHttpStatus(apiUsageService.getUsersMostUsedDomain(request), HttpStatus.OK);
    }

//    // 랭킹 유저 10명 + 사용자 별 가장 많이 사용한 도메인
//    @PreAuthorize("hasRole('OPERATION_MANAGER')")
//    @GetMapping("/all")
//    public ResponseEntity<CommonResponse<UserDomainAndRankingResponse>> getDomainAndRanking(
//            @ModelAttribute UserMostUsedDomainSearchRequest request
//    ) {
//        log.info("UserMostUsedDomainSearchRequest = {}", request);
//
//        UserDomainAndRankingResponse response = userStatisticsTupleService.getDomainAndRanking(request);
//        return ofDataWithHttpStatus(response, HttpStatus.OK);
//    }

    @PreAuthorize("hasRole('OPERATION_MANAGER')")
    @GetMapping("/{userId}/user-domain-stats")
    public ResponseEntity<CommonResponse<UserDomainStatsResponse>> getUserDomainStats(@AuthenticationPrincipal AdminDetails adminDetails, @PathVariable Long userId) {
        LocalDate startDate = LocalDate.now();
        UserDomainStatsResponse response = apiUsageService.getUserDomainStats(userId, startDate);
        return ofDataWithHttpStatus(response, HttpStatus.OK);
    }
}
