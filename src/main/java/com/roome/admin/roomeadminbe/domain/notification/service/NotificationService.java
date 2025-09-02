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
    // "내 알림 전부" 조회
    public List<NotificationResponseDto> getMyNotificationsByEmail(String email) {
        Admin me = adminRepository.findByAdminEmail(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 관리자입니다."));
        return getNotificationsByAdminId(me.getAdminId());
    }

    // 안 읽은 조회 목록 (관리자 범위)
    @Transactional(readOnly = true)
    public Map<String, Object> getUnreadGrouped(String adminEmail){
        Admin me = adminRepository.findByAdminEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 관리자입니다."));
        // 관리자 범위로 가져온 뒤, 메모리에서 unread만 필터
        List<Notification> list = adminNotificationRepository.findNotificationsByAdminId(me.getAdminId())
                .stream().filter(n -> !n.isRead())
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .toList();
        return toGroupedResponse(list);
    }

    private static final DateTimeFormatter ISO_IN_UTC = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private Map<String, Object> toGroupedResponse(List<Notification> list){
        //totalCount
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalCount", list.size());

        //날짜별 그룹
        Map<String, List<Map<String, Object>>> grouped = list.stream()
                .map(this::toItem)
                .collect(Collectors.groupingBy(
                    m -> (String) m.get("dateKey"),
                    LinkedHashMap::new,
                    Collectors.toList()
                ));
        //response 구조로 넣기
        grouped.forEach((date,items) -> {
            items.forEach(it -> it.remove("dateKey"));
            result.put(date,items);
        });

        return result;
    }

    private Map<String, Object> toItem(Notification n){
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("notificationId", n.getNotificationId());
        m.put("category", n.getCategory().name());
        m.put("message", n.getNotificationContent());
        m.put("isRead", n.isRead());
        m.put("isUrgent", n.isUrgent());

        String ts = n.getCreatedAt().atOffset(ZoneOffset.UTC).format(ISO_IN_UTC);
        m.put("timestamp", ts);

        m.put("dateKey", n.getCreatedAt().toLocalDate().toString());
        return m;
    }

    // 긴급 목록 조회 (관리자 범위 + urgent만)
    @Transactional(readOnly = true)
    public Map<String, Object> getUrgentGrouped(String adminEmail){
        Admin me = adminRepository.findByAdminEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 관리자입니다."));
        //나에게 배정된 알림 중 "긴급"만!
        List<Notification> list = adminNotificationRepository.findNotificationsByAdminId(me.getAdminId())
                .stream()
                .filter(Notification::isUrgent) // 긴급 "전체"
        //        .stream().filter(n -> n.isUrgent())//이건 미읽음 긴급
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .toList();
        return toGroupedResponse(list);
    }

    //내 알림함 구분(전체,안읽음,긴급안읽음)
    @Transactional (readOnly = true)
    public Map<String, Object> getMineGroupedWithSummary(String adminEmail){
        //1) 전체목록
        List<Notification> all = notificationRepository.findAllByOrderByCreatedAtDesc();

        //2) 카운트 먼저 계산
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
        //toItem에서 내부용으로 넣은 dateKey 제거하기
        grouped.forEach((date, items) -> {
            items.forEach(it -> it.remove("dateKey"));
            result.put(date, items);
        });

        return result;
    }


    //내 알림함 응답 시 날짜별 그룹
    private Map<String, List<Map<String, Object>>> groupByDate(List<Notification> list){
        return list.stream()
                .map(this::toItem)
                .collect(Collectors.groupingBy(
                        m -> (String) m.get("dateKey"),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
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
        dto.setUrgent(isUrgent);

        return createNotification(dto);
    }

    //단일 읽음 처리
    @Transactional
    public NotificationResponseDto markAsRead(Long notificationId, String adminEmail){
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(()-> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
        //admin 연관/컬럼 복구 후 아래 한 줄 활성화 (소유자 검증)
        // if (!n.getAdmin().getEmail().equals(adminEmail)) throw new BusinessException(ErrorCode.FORBIDDEN);

        if (!n.isRead()){
            n.setRead(true);
        }
        return new NotificationResponseDto(n);
    }
    // 전체 읽음 처리
    @Transactional
    public String markAllAsReadByAdminId(Long adminId){
        if (adminId == null) throw new IllegalArgumentException("adminId가 필요합니다.");
        notificationRepository.markAllAsReadByAdminId(adminId);
        return "전체 알림이 읽음 처리되었습니다.";
    }
}
