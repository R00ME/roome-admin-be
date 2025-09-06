package com.roome.admin.roomeadminbe.domain.ga4.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ga_event_daily")
public class GaEventDaily {
    @Id @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    private String eventName;
    private String batchId;

    @Column(name = "user_id")
    private String userId;

    private String sessionId;

    private Long sessionCount;
    private Long uniqueFeatures;
    private Long uniqueUsers;

    private LocalDate eventTime;
    private LocalDateTime collectedAt;
}

