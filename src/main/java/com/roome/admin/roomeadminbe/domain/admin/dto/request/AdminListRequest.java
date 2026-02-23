package com.roome.admin.roomeadminbe.domain.admin.dto.request;

import com.roome.admin.roomeadminbe.domain.admin.entity.AdminRole;
import com.roome.admin.roomeadminbe.domain.common.dto.request.ListRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder
public class AdminListRequest extends ListRequest {

    private AdminRole role;

    // enum 파라미터를 문자열로 받을 경우 바인딩 오류 방지
    public void setRole(String role) {
        try {
            this.role = AdminRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            this.role = null;
        }
    }
}
