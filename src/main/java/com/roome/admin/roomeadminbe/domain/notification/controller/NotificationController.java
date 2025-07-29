package com.roome.admin.roomeadminbe.domain.notification.controller;

import com.roome.admin.roomeadminbe.domain.notification.dto.NotificationRequestDto;
import com.roome.admin.roomeadminbe.domain.notification.dto.NotificationResponseDto;
import com.roome.admin.roomeadminbe.domain.notification.service.NotificationService;
import com.roome.admin.roomeadminbe.domain.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationResponseDto> getAllNotifications(){
        return notificationService.getAllNotifications();
    }
    //post API
    @PostMapping
    public ResponseEntity<Notification> crateNotification(@RequestBody NotificationRequestDto requestDto){
        Notification notification = notificationService.createNotification(requestDto);
        return ResponseEntity.ok(notification);
    }
}
