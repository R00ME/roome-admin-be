package com.roome.admin.roomeadminbe.domain.alarm.dto;

import com.roome.admin.roomeadminbe.domain.alarm.type.AlarmCategory;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AlarmRequestDto {

    private String alarmTitle; // 상단제목
    private String alarmContent; // 본문 내용
    private AlarmCategory category; //알림 분류 enum
    private boolean isUrgent; //긴급 알림 유무 true:중요, false:일반
    private Long targetUserSq;// 알림대상사용자(옵션)
}
