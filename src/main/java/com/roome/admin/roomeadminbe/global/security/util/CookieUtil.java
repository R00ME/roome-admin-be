package com.roome.admin.roomeadminbe.global.security.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;

import java.util.Arrays;

public class CookieUtil {

	private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
	private static final int REFRESH_TOKEN_EXPIRY = 7 * 24 * 60 * 60; // 7일

	public static void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
		Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
		cookie.setHttpOnly(true); // JS 접근 불가
		cookie.setSecure(true); // HTTPS에서만 전송
		cookie.setPath("/"); // 전체 경로에서 유효
		cookie.setMaxAge(REFRESH_TOKEN_EXPIRY); // 유효 시간 설정

		response.addCookie(cookie);
	}

	public static String extractRefreshTokenFromCookie(HttpServletRequest request) {
		if (request.getCookies() == null) {
			return null;
		}

		return Arrays.stream(request.getCookies())
				.filter(cookie -> REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
				.map(Cookie::getValue)
				.findFirst()
				.orElse(null);
	}
}
