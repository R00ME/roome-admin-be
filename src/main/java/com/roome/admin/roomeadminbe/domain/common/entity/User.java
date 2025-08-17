package com.roome.admin.roomeadminbe.domain.common.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

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
}
