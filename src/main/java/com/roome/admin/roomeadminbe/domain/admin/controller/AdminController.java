package com.roome.admin.roomeadminbe.domain.admin.controller;

import com.roome.admin.roomeadminbe.domain.admin.dto.request.UpdateAdminInfoRequest;
import com.roome.admin.roomeadminbe.domain.admin.dto.request.UpdatePasswordRequest;
import com.roome.admin.roomeadminbe.domain.admin.dto.response.ReadAdminInfoResponse;
import com.roome.admin.roomeadminbe.domain.admin.service.AdminService;
import com.roome.admin.roomeadminbe.domain.common.dto.response.CommonResponse;
import com.roome.admin.roomeadminbe.global.security.model.AdminDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.roome.admin.roomeadminbe.domain.common.dto.response.CommonResponse.ofDataWithHttpStatus;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/info")
public class AdminController {

	private final AdminService adminService;

	@GetMapping
	public ResponseEntity<CommonResponse<ReadAdminInfoResponse>> readInfo(@AuthenticationPrincipal AdminDetails adminDetails) {
		ReadAdminInfoResponse readAdminInfo = adminService.readInfo(adminDetails.getUsername());
		return ofDataWithHttpStatus(readAdminInfo, HttpStatus.OK);
	}

	@PatchMapping
	public ResponseEntity<CommonResponse<String>> updateInfo(@AuthenticationPrincipal AdminDetails adminDetails, @RequestBody @Validated UpdateAdminInfoRequest updateAdminInfoRequest) {
		adminService.updateInfo(adminDetails.getUsername(), updateAdminInfoRequest);
		return ofDataWithHttpStatus("관리자 정보 수정 완료", HttpStatus.OK);
	}

	@PutMapping("/password")
	public ResponseEntity<CommonResponse<String>> updatePassword(@AuthenticationPrincipal AdminDetails adminDetails, @RequestBody @Validated UpdatePasswordRequest updatePasswordRequest) {
		adminService.updatePassword(adminDetails.getUsername(), updatePasswordRequest);
		return ofDataWithHttpStatus("관리자 비밀번호 수정 완료", HttpStatus.OK);
	}
}
