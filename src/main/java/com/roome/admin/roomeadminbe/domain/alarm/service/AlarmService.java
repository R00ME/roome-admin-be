package com.roome.admin.roomeadminbe.domain.alarm.service;

import com.roome.admin.roomeadminbe.domain.alarm.dto.AlarmRequestDto;
import com.roome.admin.roomeadminbe.domain.alarm.entity.Alarm;
import com.roome.admin.roomeadminbe.domain.alarm.repository.AlarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;

    public List<Alarm> getAllAlarms(){
        return alarmRepository.findAll();
    }

    public Alarm createAlarm(AlarmRequestDto dto) {
        Alarm alarm = Alarm.builder()
                .alarmTitle(dto.getAlarmTitle())
                .alarmContent(dto.getAlarmContent())
                .category(dto.getCategory())
                .isUrgent(dto.isUrgent())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        return alarmRepository.save(alarm);
    }
}
