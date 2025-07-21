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

import java.util.NoSuchElementException;
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
        Admin admin = adminRepository.findByAdminEmail(adminEmail).orElseThrow();
        return ReadAdminInfoResponse.builder()
                .adminEmail(admin.getAdminEmail())
                .username(admin.getAdminName())
                .phoneNumber(admin.getPhoneNumber())
                .build();
    }

    public void updateInfo(String adminEmail, UpdateAdminInfoRequest updateAdminInfoRequest) {
        Admin admin = adminRepository.findByAdminEmail(adminEmail).orElseThrow();
        if (!passwordEncoder.matches(updateAdminInfoRequest.getPassword(), admin.getPassword())) {
            // exception 일괄 처리
            throw new NoSuchElementException();
        }
        admin.updateInfo(adminEmail, updateAdminInfoRequest);
    }

    public void updatePassword(String adminEmail, UpdatePasswordRequest updatePasswordRequest) {
        Admin admin = adminRepository.findByAdminEmail(adminEmail).orElseThrow();

        // 비밀번호 불일치
        if (!passwordEncoder.matches(updatePasswordRequest.getBeforePassword(), admin.getPassword())) {
            // exception 일괄 처리 예정
            throw new NoSuchElementException();
        }

        // 비밀번호, 비밀번호 확인 불일치  -> 프론트 처리시 삭제 예정
        if (!updatePasswordRequest.getConfirmPassword().equals(updatePasswordRequest.getNewPassword())) {
            throw new NoSuchElementException();
        }

        admin.updatePassword(adminEmail, passwordEncoder.encode(updatePasswordRequest.getNewPassword()));
    }

    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        String newPassword = generateRandomPassword();
        Optional<Admin> admin = adminRepository.findByAdminEmailAndAdminName(resetPasswordRequest.getConfirmEmail(), resetPasswordRequest.getConfirmName());
        if(admin.isPresent()) {
            mailService.sendNewPasswordEmail(resetPasswordRequest.getConfirmEmail(), newPassword);
        } else {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }
}
