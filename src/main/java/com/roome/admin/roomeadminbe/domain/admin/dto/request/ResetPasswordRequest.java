package com.roome.admin.roomeadminbe.domain.admin.dto.request;

import lombok.Getter;

@Getter
public class ResetPasswordRequest {

    private String confirmEmail;
    private String confirmName;
}
