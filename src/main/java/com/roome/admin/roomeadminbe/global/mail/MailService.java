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
	public void sendInvitationEmail(String toEmail) {
		try {
			String subject = "관리자 초대 메일";
			String message = """
                안녕하세요, 새로운 관리자 계정을 생성하려면 아래 링크를 클릭해주세요:

                %s

                감사합니다.
                """.formatted("https://roome-be.io.kr/login");

			SimpleMailMessage email = new SimpleMailMessage();
			email.setTo(toEmail);
			email.setSubject(subject);
			email.setText(message);

			mailSender.send(email);
			log.info("초대 메일 전송 성공 - 대상: {}", toEmail);

		} catch (Exception e) {
			log.error("초대 메일 전송 실패 - 대상: {}, 사유: {}", toEmail, e.getMessage(), e);
		}
	}

	@Async
	public void sendTempPasswordEmail(String toEmail, String tempPassword) {
		try {
			String subject = "[roome 관리자] 임시 비밀번호 안내";
			String message = """
        안녕하세요,

        요청하신 임시 비밀번호는 아래와 같습니다:

        👉 %s

        로그인 후 반드시 비밀번호를 변경해주세요.

        감사합니다.
        """.formatted(tempPassword);

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
