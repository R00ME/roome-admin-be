package com.roome.admin.roomeadminbe.domain.notification.type;

import lombok.Getter;

@Getter
public enum NotificationCategory {

    EVENT("이벤트 관리"),
    SYSTEM("운영시스템 / 배포현황"),
    NOTICE("공지사항");

    private final String displayName;

    NotificationCategory(String displayName) {
        this.displayName = displayName;
    }
}
