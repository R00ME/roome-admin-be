package com.roome.admin.roomeadminbe.domain.apiUsage.dto.response;

import lombok.Getter;

@Getter
public class DomainCountResponse {

    private String domain;
    private Long count;
    private double ratio;

    public DomainCountResponse(String domain, long count, long total) {
        this.domain = domain;
        this.count = count;
        this.ratio = (total > 0)
                ? Math.round((count * 1000.0 / total)) / 10.0
                : 0.0;
    }

    public DomainCountResponse(String key, Long value) {
        this.domain = key;
        this.count = (value != null ? value : 0L);
        this.ratio = 0.0;
    }
}
