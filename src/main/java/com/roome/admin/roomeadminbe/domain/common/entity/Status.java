package com.roome.admin.roomeadminbe.domain.common.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {
    ONLINE("온라인"), OFFLINE("오프라인");
    private final String status;
}
