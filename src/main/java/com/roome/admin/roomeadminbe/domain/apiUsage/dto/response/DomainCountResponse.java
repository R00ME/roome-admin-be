package com.roome.admin.roomeadminbe.domain.apiUsage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class DomainCountResponse {

    private String domain;
    private Long count;
    private double ratio;

    public DomainCountResponse(String domain, long count, long total) {
        this.domain = domain;
        this.count = count;
        this.ratio = (total > 0) ? (count * 100.0 / total) : 0.0;
    }

    public DomainCountResponse(String key, Long value) {
        this.domain = key;                       // 전달된 key = domain
        this.count = (value != null ? value : 0L); // 전달된 value = count
        this.ratio = 0.0;                        // total은 여기서 알 수 없으므로 기본값
    }
}
