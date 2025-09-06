package com.roome.admin.roomeadminbe.domain.event.repository;

import com.roome.admin.roomeadminbe.domain.common.dto.request.ListRequest;
import com.roome.admin.roomeadminbe.domain.event.dto.EventListResponseDTO;

import java.util.List;

public interface FirstComeEventRepositoryCustom {

    // 이벤트 목록 조회 - totalPage 조회
    Long selectTotalPagesByEvents();

    // 이벤트 목록 조회 - list
    List<EventListResponseDTO> selectAllByFirstComeEvent(ListRequest listRequest);

    // 이벤트 삭제
    void deleteFirstComeEventByEventId(Long eventId);
}
