package com.roome.admin.roomeadminbe.domain.apiUsage.entity;

import com.roome.admin.roomeadminbe.domain.common.entity.Timestamped;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "user_api_usage")
public class UserApiUsage extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private Long userId;
    private String domain;
    private String apiUri;
    private LocalDate date;
    private Long count;

    @Builder
    public UserApiUsage(Long userId, String domain, String apiUri, LocalDate date, Long count) {
        this.userId = userId;
        this.domain = domain;
        this.apiUri = apiUri;
        this.date = date;
        this.count = count;
    }
}
