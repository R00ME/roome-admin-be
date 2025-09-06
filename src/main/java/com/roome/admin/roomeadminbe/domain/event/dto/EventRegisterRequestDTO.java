package com.roome.admin.roomeadminbe.domain.event.dto;

import com.roome.admin.roomeadminbe.domain.event.entity.EventReceiverTarget;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@ToString
public class EventRegisterRequestDTO {
    private String eventTitle;
    private String eventContent;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime eventUploadTime;
    private EventReceiverTarget eventReceiverTarget;
}
