package com.roome.admin.roomeadminbe.domain.event.dto;

import com.roome.admin.roomeadminbe.domain.event.entity.EventReceiverTarget;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
public class EventListResponseDTO {
    private long eventId;
    private String eventTitle;
    private EventReceiverTarget receiverTarget;
    private LocalDateTime uploadTime;
    private String eventMessage;
    private LocalDateTime createdAt;
    private String writer;

    public EventListResponseDTO(long eventId, String eventTitle, EventReceiverTarget receiverTarget, LocalDateTime uploadTime, String eventMessage, LocalDateTime createdAt, String writer) {
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.receiverTarget = receiverTarget;
        this.uploadTime = uploadTime;
        this.eventMessage = eventMessage;
        this.createdAt = createdAt;
        this.writer = writer;
    }
}
