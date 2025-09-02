package com.roome.admin.roomeadminbe.domain.ga4.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ga_event_daily")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class GaEventDaily {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false) private LocalDate statDate;
    @Column(nullable=false) private String eventName;
    @Column private String featureName;

    @Column private Long eventCount;
    @Column private Long durationMsSum;
    @Column private Long uniqueUsersSum;
    @Column private Long sessionCountSum;
    @Column private Long rewardPointsSum;

    @Column(nullable=false) private LocalDateTime collectedAt;
}

