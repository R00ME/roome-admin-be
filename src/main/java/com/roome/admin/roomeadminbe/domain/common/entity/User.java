package com.roome.admin.roomeadminbe.domain.common.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "users") // 같은 테이블 매핑
public class User {
    @Id
    private Long id;

    private String email;
    private String name;
    private String nickname;
    private String profileImage;
    private Gender gender;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status;
}
