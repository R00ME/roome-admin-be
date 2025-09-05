package com.roome.admin.roomeadminbe.domain.notification.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.roome.admin.roomeadminbe.domain.notification.type.NotificationCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true) // FE가 모르는 필드를 보내도 무시
public class NotificationRequestDto {

    private String notificationTitle; // 상단제목
    private String notificationContent; // 본문 내용
    private NotificationCategory category; //알림 분류 enum
    private boolean isUrgent; //긴급 알림 유무 true:중요, false:일반
}
