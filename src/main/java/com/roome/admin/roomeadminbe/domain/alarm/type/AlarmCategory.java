package com.roome.admin.roomeadminbe.domain.alarm.type;

import lombok.Getter;

@Getter
public enum AlarmCategory {

    EVENT("이벤트 관리"),
    SYSTEM("운영시스템 / 배포현황"),
    NOTICE("공지사항");

    private final String displayName;

    AlarmCategory(String displayName){
        this.displayName = displayName;
    }
}
