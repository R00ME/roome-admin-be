package com.roome.admin.roomeadminbe.domain.ga4.entity;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "ga_user_pattern")
public class GaUserPattern {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private GaEventDaily event;

    private String userId;
    private int totalSessions;
    private long totalTime;

    @ElementCollection
    private Set<String> featuresUsed;
}
