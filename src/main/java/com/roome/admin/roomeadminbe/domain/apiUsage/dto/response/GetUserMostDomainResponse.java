package com.roome.admin.roomeadminbe.domain.apiUsage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetUserMostDomainResponse {

    private UserInfoResponse userInfoResponse;
    private MostUsedDomainResponse mostUsedDomainResponse;
}
