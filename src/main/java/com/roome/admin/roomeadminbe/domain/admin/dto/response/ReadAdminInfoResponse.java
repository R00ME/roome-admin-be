package com.roome.admin.roomeadminbe.domain.admin.dto.response;

import com.roome.admin.roomeadminbe.domain.admin.entity.AdminRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReadAdminInfoResponse {
    private long adminId;
    private AdminRole adminRole;
    private String adminEmail;
    private String username;
    private String phoneNumber;
}
