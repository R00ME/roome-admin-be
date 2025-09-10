package com.roome.admin.roomeadminbe.domain.notification.controller;

import com.roome.admin.roomeadminbe.domain.admin.repository.AdminRepository;
import com.roome.admin.roomeadminbe.domain.notification.dto.NotificationRequestDto;
import com.roome.admin.roomeadminbe.domain.notification.dto.NotificationResponseDto;
import com.roome.admin.roomeadminbe.domain.notification.service.NotificationService;
import com.roome.admin.roomeadminbe.domain.notification.service.SseService;
import com.roome.admin.roomeadminbe.global.exception.BusinessException;
import com.roome.admin.roomeadminbe.global.exception.enumeration.ErrorCode;
import com.roome.admin.roomeadminbe.global.security.model.AdminDetails;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final AdminRepository adminRepository;
    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);
    private final SseService sseService;


    //알림 생성(작성자 본인에게만 생성 + 실시간 푸시)
    @PostMapping
    public ResponseEntity<NotificationResponseDto> createNotification(
            @AuthenticationPrincipal AdminDetails adminDetails, @RequestBody NotificationRequestDto requestDto) {

        if (adminDetails == null) throw new RuntimeException("인증 정보가 없습니다.");

        String email = adminDetails.getUsername();

        NotificationResponseDto created = notificationService.createForMe(email, requestDto);
        return ResponseEntity.status(201).body(created);

    }


    /**
     * 2) 전체 조회(관리용)
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponseDto>> getAllNotifications(
            @AuthenticationPrincipal AdminDetails adminDetails
    ) {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    /**
     * 3) 특정 관리자 알림 조회
     */
    @GetMapping("/{adminId}")
    public ResponseEntity<List<NotificationResponseDto>> getNotificationsByAdminId(
            @AuthenticationPrincipal AdminDetails adminDetails,
            @PathVariable Long adminId) {
        return ResponseEntity.ok(notificationService.getNotificationsByAdminId(adminId));
    }

    /**
     * 4) 내 알림함 조회
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMyNotifications(
            @AuthenticationPrincipal AdminDetails adminDetails
    ) {

        if (adminDetails == null) {
            throw new RuntimeException("인증 정보가 없습니다.");
        }

        String email = adminDetails.getUsername();
        return ResponseEntity.ok(notificationService.getMineGroupedWithSummary(email));
    }


    /**
     * 5) SSE 구독: 본인 채널만 구독 가능
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal AdminDetails admin) {
        if (admin == null || admin.getAdminId() == null) {
            throw new RuntimeException("인증 정보가 없습니다.");
        }

        // 등록 + INIT 전송 + 타임아웃/정리는 서비스에서 처리
        return sseService.subscribe(admin.getAdminId());
    }

    //전체 읽음 처리
    @PutMapping("/allread")
    public ResponseEntity<Map<String, String>> markAllAsRead(
            @AuthenticationPrincipal AdminDetails adminDetails
    ) {
        if (adminDetails == null) throw new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND);
        String email = adminDetails.getUsername();

        String message = notificationService.markAllAsReadByEmail(email);

        return ResponseEntity.ok(Map.of("message", message));
    }

    //단일 읽음 조회
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Map<String, String>> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal AdminDetails adminDetails
    ) {
        if (adminDetails == null) {
            throw new RuntimeException("인증 정보가 없습니다.");
        }
        String email = adminDetails.getUsername();
        notificationService.markAsRead(notificationId, email);
        return ResponseEntity.ok(Map.of("message", "알림이 읽음 처리되었습니다."));
    }

    //안읽음조회
    @GetMapping("/unread")
    public Map<String, Object> getUnread(@AuthenticationPrincipal AdminDetails adminDetails) {
        String email = adminDetails.getUsername();
        return notificationService.getUnreadGrouped(email);
    }

    //긴급 조회
    @GetMapping("/urgent")
    public Map<String, Object> getUrgent(@AuthenticationPrincipal AdminDetails adminDetails) {
        String email = adminDetails.getUsername();
        return notificationService.getUrgentGrouped(email);
    }
}
