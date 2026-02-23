package com.roome.admin.roomeadminbe.domain.notification.type;

import lombok.Getter;

@Getter
public enum NotificationCategory {

    EVENT("이벤트 관리"),
    SYSTEM("운영시스템 관련"),
    CICD("배포/자동화 관련"),
    USER("사용자 활동 관련"),
    ETC("기타");

    private final String displayName;

    NotificationCategory(String displayName) {
        this.displayName = displayName;
    }
}
