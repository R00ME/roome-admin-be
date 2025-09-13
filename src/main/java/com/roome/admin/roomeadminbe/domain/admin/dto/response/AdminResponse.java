package com.roome.admin.roomeadminbe.domain.admin.dto.response;

import com.roome.admin.roomeadminbe.domain.admin.entity.Admin;
import com.roome.admin.roomeadminbe.domain.admin.entity.AdminRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminResponse {
    private Long id;
    private String name;
    private String email;
    private AdminRole role;
    private LocalDateTime  createdAt;

    public static AdminResponse from(Admin admin) {
        return new AdminResponse(
                admin.getAdminId(),
                admin.getAdminEmail(),
                admin.getAdminName(),
                admin.getAdminRole(),
                admin.getCreatedAt()
        );
    }
}
