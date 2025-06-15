package com.roome.admin.roomeadminbe.global.security.jwt.service;

import com.roome.admin.roomeadminbe.global.security.jwt.provider.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

import static com.roome.admin.roomeadminbe.global.security.util.CookieUtil.addRefreshTokenCookie;
import static com.roome.admin.roomeadminbe.global.security.util.CookieUtil.extractRefreshTokenFromCookie;

@Service
@RequiredArgsConstructor
public class TokenService {

	private final TokenProvider tokenProvider;
	private final RefreshTokenService refreshTokenService;

	public void refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = extractRefreshTokenFromCookie(request);

		if (refreshToken == null || !tokenProvider.validateRefreshToken(refreshToken)) {
			throw new NoSuchElementException("유효하지 않은 리프레시 토큰입니다.");
		}

		Long userId = tokenProvider.getUserFromRefreshToken(refreshToken);

		String savedToken = refreshTokenService.getRefreshToken(userId); // Redis나 DB에서 저장된 refreshToken 조회
		if (!refreshToken.equals(savedToken)) {
			throw new NoSuchElementException("저장된 토큰과 일치하지 않습니다.");
		}

		Authentication authentication = tokenProvider.getAuthenticationFromUserId(userId);

		String newAccessToken = tokenProvider.createAccessToken(authentication);

		// refreshToken rotation
		String newRefreshToken = tokenProvider.createRefreshToken(authentication);

		refreshTokenService.deleteRefreshToken(userId);
		refreshTokenService.saveRefreshToken(userId, newRefreshToken);

		// CookieUtil
		addRefreshTokenCookie(response, newRefreshToken);

		// accessToken은 응답 헤더에 담아서 보내기
		response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken);
	}
}
