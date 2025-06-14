package com.roome.admin.roomeadminbe.domain.admin.entity;

import com.roome.admin.roomeadminbe.domain.admin.dto.request.UpdateAdminInfoRequest;
import com.roome.admin.roomeadminbe.domain.common.entity.Timestamped;
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
	private AdminRole adminRole;
	private String adminName;
	private String adminEmail;
	private String password;
	private String phoneNumber;
	@Enumerated(EnumType.STRING)
	private ActivationStatus activationStatus;
	private LocalDateTime deletedAt;
	private Boolean isDeletedAt;
	@Enumerated(EnumType.STRING)
	private Status status;
	private LocalDateTime lastLoginAt;

	public boolean isActivated() {
		return this.activationStatus == ActivationStatus.ACTIVE;
	}

	public void updateTempPassword(String password) {
		this.password = password;
		this.activationStatus = ActivationStatus.ACTIVE;
	}

	public void updateInfo(String adminEmail, UpdateAdminInfoRequest updateAdminInfoRequest) {
		this.adminName = updateAdminInfoRequest.getUsername().equals("") ? adminName : updateAdminInfoRequest.getUsername();
		this.phoneNumber = updateAdminInfoRequest.getPhoneNumber().equals("") ? phoneNumber : updateAdminInfoRequest.getPhoneNumber();
	}

	public void updatePassword(String adminEmail, String encryptedNewPassword) {
		this.password = encryptedNewPassword;
	}

}
