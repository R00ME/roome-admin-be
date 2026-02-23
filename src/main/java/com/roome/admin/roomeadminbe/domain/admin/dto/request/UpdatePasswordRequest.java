package com.roome.admin.roomeadminbe.domain.admin.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class UpdatePasswordRequest {
    private String beforePassword;
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{10,}$",
            message = "비밀번호는 소문자, 숫자, 특수문자를 포함한 10자 이상이어야 합니다."
    )
    private String newPassword;
    private String confirmPassword;
}
