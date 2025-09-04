package com.roome.admin.roomeadminbe.domain.ga4.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="ga_event_daily")
@Getter
public class FeatureEventDaily {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false) private LocalDate statDate;        // 2025-08-09
    @Column(nullable=false) private String eventName;          // event_participation_success 등
    @Column private String eventCategory;                      // event_security, event 등
    @Column private String eventLabel;                         // rapid_clicks 등

    // 합계 지표
    @Column(nullable=false) private long eventCount;           // 기본
    @Column private Long valueSum;                             // value
    @Column private Long durationMsSum;                        // duration_ms
    @Column private Long sessionCountSum;                      // session_count
    @Column private Long uniqueFeaturesSum;                    // unique_features
    @Column
    private Long uniqueUsersSum;                       // unique_users
    @Column private Long rewardPointsSum;                      // reward_points (있다면)

    // 조회 필터로 자주 쓸 것들 인덱싱
    @Column private String featureName;                        // feature_usage 전용
    @Column private String userId;
    @Column private String sessionId;
    @Column private String batchId;

    // 기타 파라미터 원본(문자열화)
    @Column(columnDefinition = "json") private String paramsJson;

    @Column(nullable=false) private LocalDateTime collectedAt;
}
