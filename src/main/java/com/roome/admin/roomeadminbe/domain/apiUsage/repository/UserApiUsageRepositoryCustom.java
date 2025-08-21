package com.roome.admin.roomeadminbe.domain.apiUsage.repository;

import com.roome.admin.roomeadminbe.domain.apiUsage.dto.request.ApiUsageSearchRequest;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.ApiUsageResponse;
import com.roome.admin.roomeadminbe.domain.apiUsage.dto.response.DomainCountResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface UserApiUsageRepositoryCustom {

    Page<ApiUsageResponse> findAllBeforeDate(ApiUsageSearchRequest apiUsageSearchRequest, Pageable pageable);
    List<DomainCountResponse> findDomainCounts(Long userId, LocalDate from, LocalDate to);
//    Page<GetUserMostDomainResponse> findUsersWithMostUsedDomain(UserMostUsedDomainSearchRequest userMostUsedDomainSearchRequest, Pageable pageable);
}
