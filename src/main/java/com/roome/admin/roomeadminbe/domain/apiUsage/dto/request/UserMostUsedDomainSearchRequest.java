package com.roome.admin.roomeadminbe.domain.apiUsage.dto.request;

import com.roome.admin.roomeadminbe.domain.common.dto.request.ListRequest;
import lombok.Getter;

@Getter
public class UserMostUsedDomainSearchRequest extends ListRequest {

    private String nickName;
    private String domain;
}
