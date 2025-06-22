package com.roome.admin.roomeadminbe.domain.admin.controller;

import com.roome.admin.roomeadminbe.domain.admin.dto.request.UpdateAdminInfoRequest;
import com.roome.admin.roomeadminbe.domain.admin.dto.request.UpdatePasswordRequest;
import com.roome.admin.roomeadminbe.domain.admin.dto.response.ReadAdminInfoResponse;
import com.roome.admin.roomeadminbe.domain.admin.service.AdminService;
import com.roome.admin.roomeadminbe.global.security.model.AdminDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/v1/admin/info")
public class AdminController {

	private final AdminService adminService;

	@GetMapping
	public ResponseEntity<ReadAdminInfoResponse> readInfo(@AuthenticationPrincipal AdminDetails adminDetails) {
		ReadAdminInfoResponse readAdminInfo = adminService.readInfo(adminDetails.getUsername());

		log.info("Admin username: {}", adminDetails.getUsername());
		return ResponseEntity.ok().body(readAdminInfo);
	}

	@PatchMapping
	public ResponseEntity<Void> updateInfo(@AuthenticationPrincipal AdminDetails adminDetails, @RequestBody @Validated UpdateAdminInfoRequest updateAdminInfoRequest) {
		adminService.updateInfo(adminDetails.getUsername(), updateAdminInfoRequest);
		return ResponseEntity.ok().build();
	}

	@PutMapping("/password")
	public ResponseEntity<Void> updatePassword(@AuthenticationPrincipal UserDetails userDetails, @RequestBody @Validated UpdatePasswordRequest updatePasswordRequest) {
		adminService.updatePassword(userDetails.getUsername(), updatePasswordRequest);
		return ResponseEntity.ok().build();
	}
}
