package com.roome.admin.roomeadminbe.domain.apiUsage.repository;

import com.roome.admin.roomeadminbe.domain.apiUsage.dto.request.ApiUsageSearchRequest;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.ApiUsageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserApiUsageRepositoryCustom {

    public Page<ApiUsageResponse> findAll(ApiUsageSearchRequest apiUsageSearchRequest, Pageable pageable);
}
