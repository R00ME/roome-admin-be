package com.roome.admin.roomeadminbe.domain.admin.entity;

import com.roome.admin.roomeadminbe.domain.admin.dto.request.UpdateAdminInfoRequest;
import com.roome.admin.roomeadminbe.domain.common.entity.Timestamped;
import com.roome.admin.roomeadminbe.domain.superadmin.dto.request.InviteAdminRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "admins")
public class Admin extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;
    @Enumerated(EnumType.STRING)
    private AdminRole adminRole;
    private String adminName;
    private String adminEmail;
    private String password;
    private String phoneNumber;
    @Enumerated(EnumType.STRING)
    private ActivationStatus activationStatus;
    private LocalDateTime deletedAt;
    @Enumerated(EnumType.STRING)
    private Status status;
    private LocalDateTime lastLoginAt;

    public boolean isActivated() {
        return this.activationStatus == ActivationStatus.ACTIVE;
    }

    public void updateInfo(UpdateAdminInfoRequest updateAdminInfoRequest) {
        this.adminName = updateAdminInfoRequest.getUsername().equals("") ? adminName : updateAdminInfoRequest.getUsername();
        this.phoneNumber = updateAdminInfoRequest.getPhoneNumber().equals("") ? phoneNumber : updateAdminInfoRequest.getPhoneNumber();
    }

    public void updatePassword(String encryptedNewPassword) {
        this.password = encryptedNewPassword;
    }

    public void deleteAdminRole() {
        this.activationStatus = ActivationStatus.INACTIVE;
        this.deletedAt = LocalDateTime.now();
    }

    public void changeStatus(InviteAdminRequest inviteAdminRequest, String encodedPassword) {
        this.adminRole = inviteAdminRequest.getAdminRole();
        this.adminName = inviteAdminRequest.getAdminName();
        this.adminEmail = inviteAdminRequest.getAdminEmail();
        this.phoneNumber = inviteAdminRequest.getPhoneNumber();
        this.activationStatus = ActivationStatus.ACTIVE;
        this.password = encodedPassword;
        this.deletedAt = null;
        this.lastLoginAt = null;
        this.status = null;
    }

}
