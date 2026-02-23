package com.roome.admin.roomeadminbe.domain.admin.entity;


public enum AdminRole {
    SUPER_ADMIN,
    OPERATION_MANAGER,
    SYSTEM_MANAGER;

    public String getRoleName() {
        return "ROLE_" + this.name();
    }
}
