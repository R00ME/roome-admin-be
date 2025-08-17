package com.roome.admin.roomeadminbe.global.ga4.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class EventDailyDto {
    private LocalDate statDate;
    private String eventName;
    private String eventCategory;
    private String featureName;
    private String userId;
    private long eventCount;
    private Long valueSum;
    private Long durationMsSum;
}
