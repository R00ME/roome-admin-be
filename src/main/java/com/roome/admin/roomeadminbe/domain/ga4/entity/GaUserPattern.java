package com.roome.admin.roomeadminbe.domain.ga4.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "ga_user_pattern")
public class GaUserPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private GaEventDaily event;

    private String userId;

    private Integer totalSessions;
    private Long totalTime;

    @ElementCollection
    @CollectionTable(name = "ga_user_pattern_features", joinColumns = @JoinColumn(name = "user_pattern_id"))
    @Column(name = "feature_name")
    private Set<String> featuresUsed;
}