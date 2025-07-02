package com.roome.admin.roomeadminbe.domain.admin.dto.request;

import lombok.Getter;

@Getter
public class UpdatePasswordRequest {
    private String beforePassword;
    private String newPassword;
    private String confirmPassword;
}
