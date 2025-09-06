package com.roome.admin.roomeadminbe.domain.ga4.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "ga_event_daily")
public class GaEventDaily {
    @Id @GeneratedValue
    private Long id;

    private LocalDate statDate;
    private String eventName;
    private String batchId;

    @Column(name = "user_id")
    private String userId;   // 예약어 아니면 그대로 저장

    private String sessionId;

    private int sessionCount;
    private int uniqueFeatures;
    private int uniqueUsers;

    private LocalDateTime collectedAt;
}

