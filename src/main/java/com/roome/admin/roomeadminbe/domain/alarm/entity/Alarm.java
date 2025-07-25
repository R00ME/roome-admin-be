package com.roome.admin.roomeadminbe.domain.alarm.entity;

import com.roome.admin.roomeadminbe.domain.alarm.type.AlarmCategory;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "alarm")
public class Alarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alarm_id")
    private Long alarmId;

    @Column(name = "alarm_title", nullable = false)
    private String alarmTitle;

    @Column(name = "alarm_content", nullable = false)
    private String alarmContent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "is_urgent", nullable = false)
    private boolean isUrgent;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private AlarmCategory category;

}
