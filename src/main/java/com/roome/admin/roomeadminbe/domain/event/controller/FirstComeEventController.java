package com.roome.admin.roomeadminbe.domain.event.controller;


import com.roome.admin.roomeadminbe.domain.common.dto.request.ListRequest;
import com.roome.admin.roomeadminbe.domain.common.dto.response.CommonResponse;
import com.roome.admin.roomeadminbe.domain.common.dto.response.ListResponse;
import com.roome.admin.roomeadminbe.domain.event.dto.EventListResponseDTO;
import com.roome.admin.roomeadminbe.domain.event.dto.EventRegisterRequestDTO;
import com.roome.admin.roomeadminbe.domain.event.service.FirstComeEventService;
import com.roome.admin.roomeadminbe.global.security.model.AdminDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
public class FirstComeEventController {

	private final FirstComeEventService firstComeEventService;

	// 이벤트 목록 조회
	@GetMapping("/list")
	public ResponseEntity<?> list(@ModelAttribute ListRequest listRequest){

		ListResponse<EventListResponseDTO> list = firstComeEventService.list(listRequest);
		CommonResponse<ListResponse<EventListResponseDTO>> response = CommonResponse.success(list);
 		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// 이벤트 생성
	@PostMapping()
	public ResponseEntity<?> registerEvent(@AuthenticationPrincipal AdminDetails adminDetails, @RequestBody EventRegisterRequestDTO eventRegisterRequestDTO){

		firstComeEventService.registerEvent(eventRegisterRequestDTO, adminDetails.getUsername());

		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	// 이벤트 삭제
	@DeleteMapping()
	public ResponseEntity<?> deleteEvent(@RequestParam Long eventId){

		firstComeEventService.deleteEvent(eventId);

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
