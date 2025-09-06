package com.roome.admin.roomeadminbe.domain.ga4.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "ga_feature_stat")
public class GaFeatureStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private GaEventDaily event;

    private String featureName;

    private Integer usageCount;
    private Long totalDuration;
    private Long minDuration;
    private Long maxDuration;
    private Long averageDuration;
    private Long medianDuration;
}