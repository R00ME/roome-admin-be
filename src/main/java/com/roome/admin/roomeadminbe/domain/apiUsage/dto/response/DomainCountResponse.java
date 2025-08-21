package com.roome.admin.roomeadminbe.domain.apiUsage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DomainCountResponse {

    private String domain;
    private Long count;
}
