package com.roome.admin.roomeadminbe.domain.alarm.service;

import com.roome.admin.roomeadminbe.domain.alarm.entity.Alarm;
import com.roome.admin.roomeadminbe.domain.alarm.repository.AlarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;

    public List<Alarm> getAllAlarms(){
        return alarmRepository.findAll();
    }
}
