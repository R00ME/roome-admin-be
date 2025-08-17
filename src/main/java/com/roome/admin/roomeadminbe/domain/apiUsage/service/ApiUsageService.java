package com.roome.admin.roomeadminbe.domain.apiUsage.service;

import com.roome.admin.roomeadminbe.domain.apiUsage.dto.request.ApiUsageSearchRequest;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.ApiUsageResponse;
import com.roome.admin.roomeadminbe.domain.apiUsage.repository.UserApiUsageRepository;
import com.roome.admin.roomeadminbe.domain.common.dto.response.ListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApiUsageService {

    private final UserApiUsageRepository userApiUsageRepository;

    public ListResponse<ApiUsageResponse> getApiUsageList(ApiUsageSearchRequest request) {
        Pageable pageable = request.toPageable();
        Page<ApiUsageResponse> page = userApiUsageRepository.findAll(request, pageable);
        return ListResponse.from(page);
    }
}
