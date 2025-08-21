package com.roome.admin.roomeadminbe.domain.apiUsage.dto.request;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class UserDomainStatsRequest {

    private Long userId;
    private LocalDate startDate;

    public LocalDate getSafeStartDate() {
        return startDate != null ? startDate : LocalDate.now();
    }
}
