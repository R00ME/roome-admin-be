package com.roome.admin.roomeadminbe.domain.notification.service;

import com.roome.admin.roomeadminbe.domain.admin.entity.Admin;
import com.roome.admin.roomeadminbe.domain.admin.repository.AdminRepository;
import com.roome.admin.roomeadminbe.domain.notification.dto.NotificationRequestDto;
import com.roome.admin.roomeadminbe.domain.notification.dto.NotificationResponseDto;
import com.roome.admin.roomeadminbe.domain.notification.entity.Notification;
import com.roome.admin.roomeadminbe.domain.notification.entity.AdminNotification;
import com.roome.admin.roomeadminbe.domain.notification.repository.NotificationRepository;
import com.roome.admin.roomeadminbe.domain.notification.repository.AdminNotificationRepository;
import com.roome.admin.roomeadminbe.domain.notification.type.NotificationCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AdminNotificationRepository adminNotificationRepository;
    private final AdminRepository adminRepository;

    // 한 관리자당 SSE 연결 1개만 유지
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    // ******SSE 채널연결 + 전송******
    //구독 : 새 연결 시 기존 연결은 종료하고 교체
    public SseEmitter subscribe(Long adminId) {
        SseEmitter emitter = new SseEmitter(30L * 60 * 1000L); //30분

        // 이전 연결이 있었다면 종료
        SseEmitter old = emitters.put(adminId, emitter);
        if (old != null) {
            try { old.complete(); } catch (Exception ignore) {}
        }

        // 연결 직후 INIT 한 번 전송 (스트림 활성화)
        try { emitter.send(SseEmitter.event().name("INIT").data("ok")); } catch (Exception ignore) {}

        // 끊기면 맵에서 제거
        emitter.onCompletion(() -> emitters.remove(adminId, emitter));
        emitter.onTimeout(() -> emitters.remove(adminId, emitter));

        return emitter;
    }

    // 해당 관리자 전송
    public void sendToClient(Long adminId, NotificationResponseDto dto)    {
        SseEmitter emitter = emitters.get(adminId);
        if (emitter == null) return;

        try {
            emitter.send(SseEmitter.event().name("notification").data(dto));
        } catch (Exception ex) {
            // 실패하면 정리
            emitters.remove(adminId, emitter);
        }
    }

    //*******조회********
    // 알림 전체 조회
    public List<NotificationResponseDto> getAllNotifications() {
        List<Notification> notifications = notificationRepository.findAll();
        return notifications.stream()
                .map(NotificationResponseDto::new)
                .collect(Collectors.toList());
    }

    // 특정 관리자에게 온 알림 전부
    public List<NotificationResponseDto> getNotificationsByAdminId(Long adminId) {
        List<Notification> notifications = adminNotificationRepository.findNotificationsByAdminId(adminId);
        return notifications.stream()
                .map(NotificationResponseDto::new)
                .collect(Collectors.toList());
    }
    // (편의) 이메일로 "내 알림 전부" 조회
    public List<NotificationResponseDto> getMyNotificationsByEmail(String email) {
        Admin me = adminRepository.findByAdminEmail(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 관리자입니다."));
        return getNotificationsByAdminId(me.getAdminId());
    }

    // *****생성(작성자)*****
    // DTO(adminId 포함)를 받아 한 명에게 생성 + 즉시 푸시
    public Notification createNotification(NotificationRequestDto dto) {
        Admin admin = adminRepository.findById(dto.getAdminId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 관리자입니다."));

        Notification notification = Notification.builder()
                .notificationTitle(dto.getNotificationTitle())
                .notificationContent(dto.getNotificationContent())
                .category(dto.getCategory())
                .isUrgent(dto.isUrgent())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);

        AdminNotification adminNotification = AdminNotification.builder()
                .admin(admin)
                .notification(saved)
                .build();
        adminNotificationRepository.save(adminNotification);

        sendToClient(admin.getAdminId(), new NotificationResponseDto(saved));
        return saved;
    }

    // (오버로드) 컨트롤러/도메인에서 Admin 객체를 이미 갖고 있을 때
    public Notification createNotification(NotificationRequestDto dto, Admin admin) {
        Notification notification = Notification.builder()
                .notificationTitle(dto.getNotificationTitle())
                .notificationContent(dto.getNotificationContent())
                .category(dto.getCategory())
                .isUrgent(dto.isUrgent())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);

        AdminNotification adminNotification = AdminNotification.builder()
                .admin(admin)
                .notification(saved)
                .build();
        adminNotificationRepository.save(adminNotification);

        sendToClient(admin.getAdminId(), new NotificationResponseDto(saved));
        return saved;
    }

    // (편의) 도메인 로직에서 “작성자 본인에게만” 자동 생성할 때 사용
    public Notification createForAuthorEmail(String email,
                                             String title,
                                             String content,
                                             NotificationCategory category,
                                             boolean isUrgent) {
        Admin me = adminRepository.findByAdminEmail(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 관리자입니다."));

        NotificationRequestDto dto = new NotificationRequestDto();
        dto.setAdminId(me.getAdminId());        // 작성자 본인
        dto.setNotificationTitle(title);
        dto.setNotificationContent(content);
        dto.setCategory(category);
        // boolean 필드 셋터가 setUrgent(...) 또는 setIsUrgent(...) 중 무엇인지 DTO에 맞춰 사용하세요.
        dto.setUrgent(isUrgent);

        return createNotification(dto);
    }
}
