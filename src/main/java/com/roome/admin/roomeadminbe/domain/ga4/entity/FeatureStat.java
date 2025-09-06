package com.roome.admin.roomeadminbe.domain.ga4.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ga_feature_stat")
public class FeatureStat {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private GaEventDaily event;

    private String featureName;
    private long usageCount;
    private long totalDuration;
    private long minDuration;
    private long maxDuration;
    private long averageDuration;
    private long medianDuration;
}
