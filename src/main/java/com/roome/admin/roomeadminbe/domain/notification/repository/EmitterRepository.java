package com.roome.admin.roomeadminbe.domain.notification.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class EmitterRepository {

    //한 관리자 = 연결 1개
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter save(Long adminId, SseEmitter emitter){
        emitters.put(adminId, emitter);
        return emitter;
    }

    public Optional<SseEmitter>get(Long adminId){
        return Optional.ofNullable(emitters.get(adminId));
    }
    public void delete(Long adminId){
        emitters.remove(adminId);
    }
}
