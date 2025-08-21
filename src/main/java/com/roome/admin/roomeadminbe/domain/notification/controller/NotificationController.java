package com.roome.admin.roomeadminbe.domain.notification.controller;

import com.roome.admin.roomeadminbe.domain.admin.entity.Admin;
import com.roome.admin.roomeadminbe.domain.admin.repository.AdminRepository;
import com.roome.admin.roomeadminbe.domain.notification.dto.NotificationRequestDto;
import com.roome.admin.roomeadminbe.domain.notification.dto.NotificationResponseDto;
import com.roome.admin.roomeadminbe.domain.notification.service.NotificationService;
import com.roome.admin.roomeadminbe.domain.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final AdminRepository adminRepository;

    //알림 생성(작성자 본인에게만 생성 + 실시간 푸시)
    @PostMapping
    public ResponseEntity<NotificationResponseDto> createNotification(
            @RequestBody NotificationRequestDto requestDto){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (auth != null ? auth.getName() : null);
        if (email == null) throw new RuntimeException("인증 정보가 없습니다.");

        Admin me = adminRepository.findByAdminEmail(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 관리자입니다."));

        // 클라이언트로부터 온 adminId는 무시하고, 서버에서 본인 ID로 강제 주입
        requestDto.setAdminId(me.getAdminId());

        Notification saved = notificationService.createNotification(requestDto);
        return ResponseEntity.ok(new NotificationResponseDto(saved));
    }


    /** 2) 전체 조회(관리용) */
    @GetMapping
    public ResponseEntity<List<NotificationResponseDto>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    /** 3) 특정 관리자 알림 조회(기존 호환용) */
    @GetMapping("/{adminId}")
    public ResponseEntity<List<NotificationResponseDto>> getNotificationsByAdminId(
            @PathVariable Long adminId) {
        return ResponseEntity.ok(notificationService.getNotificationsByAdminId(adminId));
    }

    /** 4) 내 알림함 조회(프론트에 권장) */
    @GetMapping("/me")
    public ResponseEntity<List<NotificationResponseDto>> getMyNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (auth != null ? auth.getName() : null);
        if (email == null) throw new RuntimeException("인증 정보가 없습니다.");

        return ResponseEntity.ok(notificationService.getMyNotificationsByEmail(email));
    }

    /** 5) SSE 구독: 본인 채널만 구독 가능 */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestParam("adminId") Long adminId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (auth != null ? auth.getName() : null);
        if (email == null) throw new RuntimeException("인증 정보가 없습니다.");

        Admin me = adminRepository.findByAdminEmail(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 관리자입니다."));

        if (!me.getAdminId().equals(adminId)) {
            throw new RuntimeException("본인 채널만 구독할 수 있습니다.");
        }

        // 등록 + INIT 전송 + 타임아웃/정리는 서비스에서 처리
        return notificationService.subscribe(adminId);
    }

    // (선택) 구독 간편 버전: 파라미터 없이 '내 채널' 자동 구독하고 싶다면 아래를 사용
    // @GetMapping(value = "/subscribe/me", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    // public SseEmitter subscribeMe() {
    //     Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    //     String email = (auth != null ? auth.getName() : null);
    //     if (email == null) throw new RuntimeException("인증 정보가 없습니다.");
    //
    //     Admin me = adminRepository.findByAdminEmail(email)
    //             .orElseThrow(() -> new RuntimeException("존재하지 않는 관리자입니다."));
    //     return notificationService.subscribe(me.getAdminId());
    // }
}
