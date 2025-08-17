package com.roome.admin.roomeadminbe.domain.apiUsage.controller;

import com.roome.admin.roomeadminbe.domain.apiUsage.dto.request.ApiUsageSearchRequest;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.ApiUsageResponse;
import com.roome.admin.roomeadminbe.domain.apiUsage.service.ApiUsageService;
import com.roome.admin.roomeadminbe.domain.common.dto.response.ListResponse;
import com.roome.admin.roomeadminbe.global.security.model.AdminDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/usage")
public class ApiUsageController {

    private final ApiUsageService apiUsageService;

    @PreAuthorize("hasRole('OPERATION_MANAGER')")
    @GetMapping
    public ListResponse<ApiUsageResponse> getApiUsageList(@AuthenticationPrincipal AdminDetails adminDetails, @ModelAttribute ApiUsageSearchRequest request) {
        return apiUsageService.getApiUsageList(request);
    }
}
