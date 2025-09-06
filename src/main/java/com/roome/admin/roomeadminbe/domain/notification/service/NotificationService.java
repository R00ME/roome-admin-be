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
import com.roome.admin.roomeadminbe.global.exception.BusinessException;
import com.roome.admin.roomeadminbe.global.exception.enumeration.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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

    //*********공통 : 정렬/타입존 유틸
    private static final ZoneOffset UTC = ZoneOffset.UTC;
    private static final DateTimeFormatter ISO_IN_UTC = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /** createdAt desc, tie-break notificationId desc */
    private static final Comparator<Notification> LATEST_FIRST =
            Comparator.comparing(Notification::getCreatedAt).reversed()
                    .thenComparing(Comparator.comparing(Notification::getNotificationId).reversed());

    private List<Notification> sortLatest(List<Notification> list) {
        return list.stream().sorted(LATEST_FIRST).toList();
    }

    private Map<String, Object> toItem(Notification n){
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("notificationId", n.getNotificationId());
        m.put("category", n.getCategory().name());
        m.put("message", n.getNotificationContent());
        m.put("isRead", n.isRead());
        m.put("isUrgent", n.isUrgent());

        // 표시와 그룹 키 모두 UTC 기준으로 통일
        String ts = n.getCreatedAt().atOffset(ZoneOffset.UTC).format(ISO_IN_UTC);
        m.put("timestamp", ts);
        m.put("dateKey",n.getCreatedAt().atOffset(UTC).toLocalDate().toString());
        return m;
    }

    private Map<String, Object> toGroupedResponse(List<Notification> list){
        //totalCount
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalCount", list.size());

        //날짜별 그룹
        Map<String, List<Map<String, Object>>> grouped = groupByDate(list);
        grouped.forEach((date, items) -> {
            items.forEach(it -> it.remove("dateKey"));
            result.put(date, items);
        });
        return result;
    }

    //*******조회********
    // 알림 전체 조회
    public List<NotificationResponseDto> getAllNotifications() {
        return sortLatest(notificationRepository.findAll())
                .stream().map(NotificationResponseDto::new).toList();
    }

    // 특정 관리자에게 온 알림 전부
    public List<NotificationResponseDto> getNotificationsByAdminId(Long adminId) {
        return adminNotificationRepository
                .findNotificationsByAdminIdOrderByCreatedAtDesc(adminId)
                .stream().map(NotificationResponseDto::new)
                .toList();
    }

    // "내 알림 전부" 조회 (알림한)
    public Map<String, Object> getMineGroupedWithSummary(String adminEmail){
        Admin me = adminRepository.findByAdminEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 관리자입니다."));

        //1)전체목록
        List<Notification> all = adminNotificationRepository
                .findNotificationsByAdminIdOrderByCreatedAtDesc(me.getAdminId());

        //2)카운트 먼저 계산
        long total = all.size();
        long unread = all.stream().filter(n -> !n.isRead()).count();
        long urgentUnread = all.stream().filter(n -> !n.isRead() && n.isUrgent()).count();

        //3) 응답 맵을 카운트 먼저 삽입 후 생성(삽입 순서 보장 위해 LinkedHashMap 사용)
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalCount", total);
        result.put("unreadCount", unread);
        result.put("urgentCount", urgentUnread);

        //4) 날짜별 그룹 만든 후 카운트 뒤에 붙이기
        Map<String, List<Map<String, Object>>> grouped = groupByDate(all);
        grouped.forEach((date, items) -> {
            items.forEach(it -> it.remove("dateKey"));
            result.put(date, items);
        });
        return result;
    }

    // 안 읽은 조회 목록 (관리자 범위)
    @Transactional(readOnly = true)
    public Map<String, Object> getUnreadGrouped(String adminEmail){
        Admin me = adminRepository.findByAdminEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 관리자입니다."));
        // 관리자 범위로 가져온 뒤, 메모리에서 unread만 필터
        List<Notification> list = adminNotificationRepository
                .findNotificationsByAdminIdOrderByCreatedAtDesc(me.getAdminId())
                .stream().filter(n -> !n.isRead())
                .toList();

        return toGroupedResponse(list);
    }


    //내 알림함 응답 시 날짜별 그룹
    private Map<String, List<Map<String, Object>>> groupByDate(List<Notification> list){
        return list.stream()
                .map(this::toItem)
                .collect(Collectors.groupingBy(
                        it-> (String) it.get("dateKey"),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    // 긴급 목록 조회 (관리자 범위 + urgent만)
    @Transactional(readOnly = true)
    public Map<String, Object> getUrgentGrouped(String adminEmail){
        Admin me = adminRepository.findByAdminEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 관리자입니다."));

        List<Notification> list = adminNotificationRepository
                .findNotificationsByAdminIdOrderByCreatedAtDesc(me.getAdminId())
                .stream().filter(Notification::isUrgent)
                .toList();

        return toGroupedResponse(list);
    }

    // *****생성(작성자)*****
    @Transactional
    public NotificationResponseDto createForMe(String email, NotificationRequestDto req){
        Admin me = adminRepository.findByAdminEmail(email)
                .orElseThrow(()-> new RuntimeException("존재하지 않는 관리자입니다."));

        Notification notification = Notification.builder()
                .notificationTitle(req.getNotificationTitle())
                .notificationContent(req.getNotificationContent())
                .category(req.getCategory())
                .isUrgent(Boolean.TRUE.equals(req.isUrgent()))
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);

        adminNotificationRepository.save(
                AdminNotification.builder().admin(me).notification(saved).build()
        );

        sendToClient(me.getAdminId(), new NotificationResponseDto(saved));
        return new NotificationResponseDto(saved);
    }

    // (편의) 내부 도메인에서 이메일만 알고 바로 생성하고 싶을 때
    public Notification createForAuthorEmail(String email,
                                             String title,
                                             String content,
                                             NotificationCategory category,
                                             boolean isUrgent) {

        NotificationRequestDto dto = new NotificationRequestDto();
        dto.setNotificationTitle(title);
        dto.setNotificationContent(content);
        dto.setCategory(category);
        dto.setUrgent(isUrgent);

        NotificationResponseDto res = createForMe(email, dto);
        return notificationRepository.findById(res.getNotificationId())
                .orElseThrow(() -> new RuntimeException("생성된 알림을 찾을 수 없습니다."));
    }

    //******읽음********
    //단일 읽음 처리
    @Transactional
    public NotificationResponseDto markAsRead(Long notificationId, String adminEmail){
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(()-> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
        //admin 연관/컬럼 복구 시 소유자 검증 추가
        if (!n.isRead()) n.setRead(true);
        return new NotificationResponseDto(n);
    }
    // 전체 읽음 처리
    @Transactional
    public String markAllAsReadByEmail(String adminEmail){
        Admin me = adminRepository.findByAdminEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 관리자입니다."));
        notificationRepository.markAllAsReadByAdminId(me.getAdminId());
        return "전체 알림이 읽음 처리되었습니다.";
    }
}
