package com.roome.admin.roomeadminbe.domain.superadmin.dto.request;

import com.roome.admin.roomeadminbe.domain.admin.entity.AdminRole;
import jakarta.validation.constraints.Email;
import lombok.Getter;

@Getter
public class InviteAdminRequest {

    private AdminRole adminRole;
    private String adminName;
    @Email
    private String adminEmail;
    private String phoneNumber;
}
