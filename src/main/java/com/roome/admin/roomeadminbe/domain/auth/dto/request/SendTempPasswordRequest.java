package com.roome.admin.roomeadminbe.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Getter;

@Getter
public class SendTempPasswordRequest {

    @Email
    private String adminEmail;
}
