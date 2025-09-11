package com.roome.admin.roomeadminbe.domain.ga4.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "ga_event_daily")
public class GaEventDaily {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate statDate;   // 수집 기준 날짜
    private String eventName;     // 이벤트 이름
    private String featureName;
    private String customUserId;  // 프론트에서 넘긴 사용자 식별자

    private String eventCategory;
    private String source; // 유입 소스 URL
    private String medium; // 유입 매체

    private Long eventCount;      // 이벤트 발생 횟수
    private Long engagementDuration; // userEngagementDuration 합산값
    private Long durationSec;

    private LocalDateTime eventAt;
    private LocalDateTime collectedAt; // 백엔드에서 수집 시각
    private String source;        // URL

    public void update(Long eventCount, Long durationSec, LocalDateTime eventAt, LocalDateTime collectedAt) {
        this.eventCount = eventCount;
        this.durationSec = durationSec;
        this.eventAt = eventAt;
        this.collectedAt = collectedAt;
    }
}
