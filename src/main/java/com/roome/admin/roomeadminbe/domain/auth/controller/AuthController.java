package com.roome.admin.roomeadminbe.domain.auth.controller;

import com.roome.admin.roomeadminbe.domain.auth.dto.TokenResponseDto;
import com.roome.admin.roomeadminbe.domain.auth.dto.request.LoginRequest;
import com.roome.admin.roomeadminbe.domain.auth.dto.request.SendTempPasswordRequest;
import com.roome.admin.roomeadminbe.domain.auth.service.AuthService;
import com.roome.admin.roomeadminbe.domain.common.dto.response.CommonResponse;
import com.roome.admin.roomeadminbe.global.security.jwt.filter.JwtFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

import static com.roome.admin.roomeadminbe.domain.common.dto.response.CommonResponse.ofDataWithHttpStatus;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/auth")
public class AuthController {

    private final AuthService authService;

    // 임시 password 발급 (첫 로그인)
    @PostMapping("/password")
    public ResponseEntity<CommonResponse<String>> sendTempPassword(@RequestBody SendTempPasswordRequest sendTempPasswordRequest) {
        authService.sendTempPassword(sendTempPasswordRequest);
        return ofDataWithHttpStatus("임시 비밀번호 발급 완료: 이메일 확인", HttpStatus.OK);
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<Void> authorize(@RequestBody @Validated LoginRequest loginRequestDto, HttpServletResponse response) {
        TokenResponseDto tokenResponseDto = authService.login(loginRequestDto);

        // accessToken -> header
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + tokenResponseDto.getAccessToken());

        // refreshToken -> HttpOnly 쿠키
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokenResponseDto.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(14))
                .sameSite("Strict")
                .build();
        response.setHeader("Set-Cookie", refreshCookie.toString());

        return new ResponseEntity<>(httpHeaders, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<String>> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ofDataWithHttpStatus("로그아웃 완료", HttpStatus.NO_CONTENT);
    }
}
