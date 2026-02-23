package com.roome.admin.roomeadminbe.domain.admin.service;

import com.roome.admin.roomeadminbe.domain.admin.dto.request.ResetPasswordRequest;
import com.roome.admin.roomeadminbe.domain.admin.dto.request.UpdateAdminInfoRequest;
import com.roome.admin.roomeadminbe.domain.admin.dto.request.UpdatePasswordRequest;
import com.roome.admin.roomeadminbe.domain.admin.dto.response.ReadAdminInfoResponse;
import com.roome.admin.roomeadminbe.domain.admin.entity.Admin;
import com.roome.admin.roomeadminbe.domain.admin.repository.AdminRepository;
import com.roome.admin.roomeadminbe.global.exception.BusinessException;
import com.roome.admin.roomeadminbe.global.exception.enumeration.ErrorCode;
import com.roome.admin.roomeadminbe.global.mail.MailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.roome.admin.roomeadminbe.global.mail.RandomPasswordGenerator.generateRandomPassword;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public ReadAdminInfoResponse readInfo(String adminEmail) {
        Admin admin = existAdmin(adminEmail);
        return ReadAdminInfoResponse.builder()
                .adminId(admin.getAdminId())
                .adminEmail(admin.getAdminEmail())
                .username(admin.getAdminName())
                .phoneNumber(admin.getPhoneNumber())
                .adminRole(admin.getAdminRole())
                .build();
    }

    public void updateInfo(String adminEmail, UpdateAdminInfoRequest updateAdminInfoRequest) {
        Admin admin = existAdmin(adminEmail);
        admin.updateInfo(updateAdminInfoRequest);
    }

    public void updatePassword(String adminEmail, UpdatePasswordRequest updatePasswordRequest) {
        Admin admin = existAdmin(adminEmail);

        if (!passwordEncoder.matches(updatePasswordRequest.getBeforePassword(), admin.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_NOT_MATCHES);
        }

        // 비밀번호, 비밀번호 확인 불일치  -> 프론트 처리시 삭제 예정
        if (!updatePasswordRequest.getConfirmPassword().equals(updatePasswordRequest.getNewPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_NOT_MATCHES);
        }

        System.out.println("비밀번호 변경 전 DB 확인: " + passwordEncoder.matches(updatePasswordRequest.getBeforePassword(), admin.getPassword()));
        admin.updatePassword(passwordEncoder.encode(updatePasswordRequest.getNewPassword()));
        System.out.println("비밀번호 변경 후 DB 확인 : " + passwordEncoder.matches(updatePasswordRequest.getNewPassword(), admin.getPassword()));
    }

    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        String newPassword = generateRandomPassword();

        Optional<Admin> admin = adminRepository.findByAdminEmailAndAdminName(resetPasswordRequest.getConfirmEmail(), resetPasswordRequest.getConfirmName());
        if (admin.isEmpty()) throw new BusinessException(ErrorCode.USER_NOT_FOUND);

        mailService.sendNewPasswordEmail(resetPasswordRequest.getConfirmEmail(), newPassword);
        admin.get().updatePassword(passwordEncoder.encode(newPassword));
    }

    private Admin existAdmin(String adminEmail) {
        Optional<Admin> admin = adminRepository.findByAdminEmail(adminEmail);
        if (admin.isEmpty()) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        return admin.get();
    }
}
