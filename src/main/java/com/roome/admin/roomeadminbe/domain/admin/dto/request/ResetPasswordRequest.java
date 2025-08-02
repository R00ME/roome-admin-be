package com.roome.admin.roomeadminbe.domain.admin.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Getter;

@Getter
public class ResetPasswordRequest {

    @Email
    private String confirmEmail;
    private String confirmName;
}
