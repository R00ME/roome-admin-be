package com.roome.admin.roomeadminbe.domain.event.service;


import com.roome.admin.roomeadminbe.domain.common.dto.request.ListRequest;
import com.roome.admin.roomeadminbe.domain.common.dto.response.ListResponse;
import com.roome.admin.roomeadminbe.domain.event.dto.EventListResponseDTO;
import com.roome.admin.roomeadminbe.domain.event.dto.EventRegisterRequestDTO;
import com.roome.admin.roomeadminbe.domain.event.entity.EventStatus;
import com.roome.admin.roomeadminbe.domain.event.entity.FirstComeEvent;
import com.roome.admin.roomeadminbe.domain.event.repository.FirstComeEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FirstComeEventServiceImpl implements FirstComeEventService{

	private final FirstComeEventRepository firstComeEventRepository;


	// 이벤트 목록 조회
	@Override
	public ListResponse<EventListResponseDTO> list(ListRequest listRequest) {

		Long selectTotalPagesByEvents = firstComeEventRepository.selectTotalPagesByEvents();
		if(selectTotalPagesByEvents == null){
			selectTotalPagesByEvents = 0L;
		}
		List<EventListResponseDTO> selectAllByFirstComeEvent = firstComeEventRepository.selectAllByFirstComeEvent(listRequest);

		Page page = new PageImpl<>(selectAllByFirstComeEvent, listRequest.toPageable(), selectTotalPagesByEvents);

		ListResponse<EventListResponseDTO> listResponse = ListResponse.from(page);

		return listResponse;

	}

	// 이벤트 생성
	@Override
	public void registerEvent(EventRegisterRequestDTO eventRegisterRequestDTO, String userName) {

		// 관리자 이름의 경우, Token으로 값을 빼오기 때문에
		// Controller에서 `@AuthenticationPrincipal UserDetails userDetails`를 사용하여 userName으로 값을 빼온다.
		firstComeEventRepository.save(
				FirstComeEvent.builder()
						.eventName(eventRegisterRequestDTO.getEventTitle())
						.eventContent(eventRegisterRequestDTO.getEventContent())
						.eventTime(eventRegisterRequestDTO.getStartDate())
						.eventEndTime(eventRegisterRequestDTO.getEndDate())
						.eventUploadTime(eventRegisterRequestDTO.getEventUploadTime())
						.eventReceiverTarget(eventRegisterRequestDTO.getEventReceiverTarget())
						.eventWriter(userName)
						.status(EventStatus.NOTYET)
						.build()
		);
	}

	// 이벤트 삭제
	@Override
	public void deleteEvent(Long eventId) {
		firstComeEventRepository.deleteFirstComeEventByEventId(eventId);
	}
}
