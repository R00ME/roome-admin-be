package com.roome.admin.roomeadminbe.domain.superadmin.service;

import com.roome.admin.roomeadminbe.domain.admin.dto.request.AdminListRequest;
import com.roome.admin.roomeadminbe.domain.admin.dto.response.AdminListResponse;
import com.roome.admin.roomeadminbe.domain.admin.dto.response.AdminResponse;
import com.roome.admin.roomeadminbe.domain.admin.entity.ActivationStatus;
import com.roome.admin.roomeadminbe.domain.admin.entity.Admin;
import com.roome.admin.roomeadminbe.domain.admin.repository.AdminRepository;
import com.roome.admin.roomeadminbe.domain.superadmin.dto.request.InviteAdminRequest;
import com.roome.admin.roomeadminbe.global.exception.BusinessException;
import com.roome.admin.roomeadminbe.global.exception.enumeration.ErrorCode;
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

    public void inviteAdmin(InviteAdminRequest inviteAdminRequest) {

        String tempPassword = generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(tempPassword);
        String checkEmail = inviteAdminRequest.getAdminEmail();

        if (adminRepository.existsByAdminEmail(checkEmail)) {
            Admin admin = adminRepository.findByAdminEmail(checkEmail).orElse(null);
            assert admin != null;
            if (admin.getActivationStatus().equals(ActivationStatus.ACTIVE)) {
                throw new BusinessException(ErrorCode.EXISTS_ADMIN);
            } else {
                admin.changeStatus(inviteAdminRequest, encodedPassword);
                mailService.sendInvitationWithTempPasswordEmail(inviteAdminRequest.getAdminEmail(), tempPassword);
                return;
            }
        }

        Admin newAdmin = Admin.builder()
                .adminRole(inviteAdminRequest.getAdminRole())
                .adminName(inviteAdminRequest.getAdminName())
                .adminEmail(inviteAdminRequest.getAdminEmail())
                .phoneNumber(inviteAdminRequest.getPhoneNumber())
                .activationStatus(ActivationStatus.ACTIVE)
                .password(passwordEncoder.encode(tempPassword))
                .deletedAt(null)
                .lastLoginAt(null)
                .status(null)
                .build();
        adminRepository.save(newAdmin);
        mailService.sendInvitationWithTempPasswordEmail(inviteAdminRequest.getAdminEmail(), tempPassword);
    }

    public AdminListResponse getAdminList(AdminListRequest adminListRequest) {
        try {
            Pageable pageable = adminListRequest.toPageable();

            Page<AdminResponse> page = adminRepository.findAll(adminListRequest, pageable);

            return AdminListResponse.from(page);
        } catch (IllegalArgumentException e) {
            // 잘못된 요청 파라미터
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        } catch (Exception e) {
            // 그 외 예기치 못한 오류
            throw new BusinessException(ErrorCode.UNHANDLED_EXCEPTION);
        }
    }

    public void deleteAdminRole(Long adminId) {
        Admin admin = adminRepository.findById(adminId).orElseThrow();
        admin.deleteAdminRole();
    }
}
