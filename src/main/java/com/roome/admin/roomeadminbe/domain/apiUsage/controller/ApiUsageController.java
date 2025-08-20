package com.roome.admin.roomeadminbe.domain.apiUsage.controller;

import com.roome.admin.roomeadminbe.domain.apiUsage.dto.request.ApiUsageSearchRequest;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.request.UserMostUsedDomainSearchRequest;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.ApiUsageResponse;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.GetUserMostDomainResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.roome.admin.roomeadminbe.domain.common.dto.response.CommonResponse.ofDataWithHttpStatus;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin/usage")
public class ApiUsageController {

    private final ApiUsageService apiUsageService;

    @PreAuthorize("hasRole('OPERATION_MANAGER')")
    @GetMapping
    public ResponseEntity<CommonResponse<ListResponse<ApiUsageResponse>>> getApiUsageList(@AuthenticationPrincipal AdminDetails adminDetails, @ModelAttribute ApiUsageSearchRequest request) {
        return ofDataWithHttpStatus(apiUsageService.getApiUsageList(request), HttpStatus.OK);
    }

    @GetMapping("/most-used-domain")
    public ResponseEntity<CommonResponse<ListResponse<GetUserMostDomainResponse>>> getUsersMostUsedDomain(
            @ModelAttribute UserMostUsedDomainSearchRequest request) {

        return ofDataWithHttpStatus(apiUsageService.getUsersMostUsedDomain(request), HttpStatus.OK);
    }
}
