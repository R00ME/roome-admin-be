package com.roome.admin.roomeadminbe.domain.apiUsage.dto.response;

import com.roome.admin.roomeadminbe.domain.common.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserInfoResponse {

    private Long userId;
    private String email;
    private String nickname;
    private Gender gender;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
}
