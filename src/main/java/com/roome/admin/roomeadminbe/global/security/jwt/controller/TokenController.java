package com.roome.admin.roomeadminbe.global.security.jwt.controller;

import com.roome.admin.roomeadminbe.domain.common.dto.response.CommonResponse;
import com.roome.admin.roomeadminbe.global.security.jwt.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.roome.admin.roomeadminbe.domain.common.dto.response.CommonResponse.ofDataWithHttpStatus;

@RestController
@RequiredArgsConstructor
public class TokenController {

	private final TokenService tokenService;

	@PostMapping("/refresh")
	public ResponseEntity<CommonResponse<String>> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
		tokenService.refreshAccessToken(request, response);
		return ofDataWithHttpStatus("accessToken 재발급 완료", HttpStatus.OK);
	}
}
