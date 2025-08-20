package com.roome.admin.roomeadminbe.domain.apiUsage.repository;

import com.roome.admin.roomeadminbe.domain.apiUsage.dto.request.ApiUsageSearchRequest;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.request.UserMostUsedDomainSearchRequest;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.ApiUsageResponse;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.GetUserMostDomainResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserApiUsageRepositoryCustom {

    Page<ApiUsageResponse> findAllBeforeDate(ApiUsageSearchRequest apiUsageSearchRequest, Pageable pageable);
//    Page<GetUserMostDomainResponse> findUsersWithMostUsedDomain(UserMostUsedDomainSearchRequest userMostUsedDomainSearchRequest, Pageable pageable);
}
