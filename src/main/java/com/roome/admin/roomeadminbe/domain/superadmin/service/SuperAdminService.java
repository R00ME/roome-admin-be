package com.roome.admin.roomeadminbe.domain.superadmin.service;

import com.roome.admin.roomeadminbe.domain.admin.dto.request.AdminListRequest;
import com.roome.admin.roomeadminbe.domain.admin.dto.response.AdminListResponse;
import com.roome.admin.roomeadminbe.domain.admin.dto.response.AdminResponse;
import com.roome.admin.roomeadminbe.domain.admin.entity.ActivationStatus;
import com.roome.admin.roomeadminbe.domain.admin.entity.Admin;
import com.roome.admin.roomeadminbe.domain.admin.repository.AdminRepository;
import com.roome.admin.roomeadminbe.domain.superadmin.dto.request.InviteAdminRequest;
import com.roome.admin.roomeadminbe.global.mail.MailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.roome.admin.roomeadminbe.global.mail.RandomPasswordGenerator.generateRandomPassword;

@Service
@Transactional
@RequiredArgsConstructor
public class SuperAdminService {

    private final MailService mailService;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public void inviteAdmin(InviteAdminRequest inviteAdminRequestDto) {

        String tempPassword = generateRandomPassword();

        Admin newAdmin = Admin.builder()
                .adminRole(inviteAdminRequestDto.getAdminRole())
                .adminName(inviteAdminRequestDto.getAdminName())
                .adminEmail(inviteAdminRequestDto.getAdminEmail())
                .phoneNumber(inviteAdminRequestDto.getPhoneNumber())
                .activationStatus(ActivationStatus.ACTIVE)
                .password(passwordEncoder.encode(tempPassword))
                .deletedAt(null)
                .isDeletedAt(false)
                .lastLoginAt(null)
                .status(null)
                .build();
        adminRepository.save(newAdmin);
        mailService.sendInvitationWithTempPasswordEmail(inviteAdminRequestDto.getAdminEmail(), tempPassword);
    }

    public AdminListResponse getAdminList(AdminListRequest adminListRequest) {
        Pageable pageable = adminListRequest.toPageable();

        Page<AdminResponse> page = adminRepository.findAll(adminListRequest, pageable);

        return AdminListResponse.from(page);
    }

    public void deleteAdminRole(Long adminId) {
        Admin admin = adminRepository.findById(adminId).orElseThrow();
        admin.deleteAdminRole();
    }
}
