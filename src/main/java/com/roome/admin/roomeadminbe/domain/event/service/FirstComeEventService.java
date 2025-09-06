package com.roome.admin.roomeadminbe.domain.event.service;

import com.roome.admin.roomeadminbe.domain.common.dto.request.ListRequest;
import com.roome.admin.roomeadminbe.domain.common.dto.response.ListResponse;
import com.roome.admin.roomeadminbe.domain.event.dto.EventListResponseDTO;
import com.roome.admin.roomeadminbe.domain.event.dto.EventRegisterRequestDTO;

public interface FirstComeEventService {

    // 이벤트 목록 조회
    ListResponse<EventListResponseDTO> list(ListRequest listRequest);

    // 이벤트 생성
    void registerEvent(EventRegisterRequestDTO eventRegisterRequestDTO, String userName);

    // 이벤트 삭제
    void deleteEvent(Long eventId);
}
