package com.roome.admin.roomeadminbe.global.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendInvitationWithTempPasswordEmail(String toEmail, String tempPassword) {
        try {
            String subject = "[roome 관리자] 계정 초대 및 임시 비밀번호 안내";
            String message = """
                    안녕하세요,

                    하단 임시 비밀번호로 로그인하세요.:

                    👉 %s

                    임시 비밀번호: %s

                    로그인 후 반드시 비밀번호를 변경해주세요.
                    
                    감사합니다.
                    """.formatted("https://roome-be.io.kr/login", tempPassword);

            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(toEmail);
            email.setSubject(subject);
            email.setText(message);

            mailSender.send(email);
            log.info("초대+임시 비밀번호 메일 전송 성공 - 대상: {}", toEmail);

        } catch (Exception e) {
            log.error("초대+임시 비밀번호 메일 전송 실패 - 대상: {}, 사유: {}", toEmail, e.getMessage(), e);
        }
    }

    @Async
    public void sendNewPasswordEmail(String toEmail, String tempPassword) {
        try {
            String subject = "[roome 관리자] 임시 비밀번호 발급";
            String message = """
                    안녕하세요,

                    하단 임시 비밀번호로 로그인하세요.:

                    👉 %s

                    임시 비밀번호: %s

                    로그인 후 반드시 비밀번호를 변경해주세요.
                    
                    감사합니다.
                    """.formatted("https://roome-be.io.kr/login", tempPassword);

            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(toEmail);
            email.setSubject(subject);
            email.setText(message);

            mailSender.send(email);
            log.info("임시 비밀번호 메일 전송 성공 - 대상: {}", toEmail);

        } catch (Exception e) {
            log.error("임시 비밀번호 메일 전송 실패 - 대상: {}, 사유: {}", toEmail, e.getMessage(), e);
        }
    }
}
