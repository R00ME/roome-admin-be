package com.roome.admin.roomeadminbe.domain.ga4.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ga_event_raw")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class GaEventRaw {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false) private String eventName;
    @Column private String featureName;
    @Column private String userId;
    @Column private String sessionId;
    @Column private String batchId;

    @Column private Long durationMs;
    @Column private Long value;
    @Column private Long rewardPoints;

    @Column(columnDefinition = "json")
    private String paramsJson;

    @Column(nullable=false) private LocalDateTime collectedAt;
}
