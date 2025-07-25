package com.roome.admin.roomeadminbe.domain.alarm.controller;

import com.roome.admin.roomeadminbe.domain.alarm.dto.AlarmRequestDto;
import com.roome.admin.roomeadminbe.domain.alarm.service.AlarmService;
import com.roome.admin.roomeadminbe.domain.alarm.entity.Alarm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/alarms")
@RequiredArgsConstructor
public class AlarmController {
    private final AlarmService alarmService;

    @GetMapping
    public List<Alarm> getAllAlarms(){
        return alarmService.getAllAlarms();
    }

    //post API
    @PostMapping
    public ResponseEntity<Alarm> crateAlarm(@RequestBody AlarmRequestDto requestDto){
        Alarm alarm = alarmService.createAlarm(requestDto);
        return ResponseEntity.ok(alarm);
    }
}
