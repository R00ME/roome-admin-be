package com.roome.admin.roomeadminbe.domain.ga4.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "ga_feature_stat")
public class GaFeatureStat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String eventName;     // 이벤트명
    private LocalDate statDate;   // 집계 일자
    private String featureName;   // 기능명
    private Long totalUsers;      // 기능을 사용한 유저 수
    private Long totalDuration;   // 총 사용시간(초)
    private Long eventCount;      // 이벤트 발생 수
    private Double avgDuration;   // 평균 사용시간(초)
}