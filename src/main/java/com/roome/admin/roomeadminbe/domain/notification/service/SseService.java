package com.roome.admin.roomeadminbe.domain.notification.service;

import com.roome.admin.roomeadminbe.domain.notification.dto.NotificationResponseDto;
import com.roome.admin.roomeadminbe.domain.notification.repository.EmitterRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class SseService {

    private static final Logger log = LoggerFactory.getLogger(SseService.class);
    private static final long TIMEOUT = 30 * 60 * 1000L; //30분

    private final EmitterRepository emitterRepository;

    /**
     * 한 관리자 = 연결 1개. 기존 연결이 있으면 닫고 교체
     */
    public SseEmitter subscribe(Long adminId) {
        emitterRepository.get(adminId).ifPresent(old -> {
            try {
                old.complete();
            } catch (Exception ignore) {
            }
            emitterRepository.delete(adminId);
            log.info("[SSE] existing emitter closed (adminId={})", adminId);
        });
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitter.onTimeout(() -> cleanup(adminId, "timeout"));
        emitter.onCompletion(() -> cleanup(adminId, "completed"));
        emitter.onError(ex -> cleanup(adminId, "error:" + ex.getClass().getSimpleName()));

        emitterRepository.save(adminId, emitter);

        // 초기 코멘트(연결 확인 및 일부 프록시 버퍼링 회피)
        try {
            emitter.send(SseEmitter.event().comment("connected"));
        } catch (IOException ignore) {
        }
        return emitter;
    }

    /**
     * 특정 관리자에게만 전송 (브로드캐스트 아님)
     */
    public void send(Long adminId, NotificationResponseDto dto) {
        emitterRepository.get(adminId).ifPresentOrElse(emitter -> {
            try {
                SseEmitter.SseEventBuilder event = SseEmitter.event()
                        .id(UUID.randomUUID().toString())
                        .name("NOTIFICATION")
                        .data(dto, MediaType.APPLICATION_JSON)
                        .reconnectTime(3000);
                emitter.send(event);
            } catch (IOException | IllegalStateException e) {
                try {
                    emitter.completeWithError(e);
                } catch (Exception ignore) {
                }
                cleanup(adminId, "send-failed");
            }
        }, () -> log.info("[SSE] no active emitter (adminId={})", adminId));
    }

    public void sendToClient(Long adminId, NotificationResponseDto dto) {
        send(adminId, dto);
    }

    private void cleanup(Long adminId, String reason) {
        emitterRepository.delete(adminId);
        log.info("[SSE] cleaned emitter (adminId={}, reason={})", adminId, reason);
    }
}