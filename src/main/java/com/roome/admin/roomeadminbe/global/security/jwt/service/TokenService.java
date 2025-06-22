package com.roome.admin.roomeadminbe.global.security.jwt.service;

import com.roome.admin.roomeadminbe.global.security.jwt.provider.TokenProvider;
import com.roome.admin.roomeadminbe.global.security.model.AdminDetails;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;

import static com.roome.admin.roomeadminbe.global.security.util.CookieUtil.addRefreshTokenCookie;
import static com.roome.admin.roomeadminbe.global.security.util.CookieUtil.extractRefreshTokenFromCookie;

@Service
public class TokenService {

	private final TokenProvider tokenProvider;
	private final RefreshTokenService refreshTokenService;
	@Qualifier("blacklistRedisTemplate")
	private final RedisTemplate<String, Long> blacklistRedisTemplate;

	public TokenService(
			TokenProvider tokenProvider, RefreshTokenService refreshTokenService, @Qualifier("blacklistRedisTemplate") RedisTemplate<String, Long> blacklistRedisTemplate
	) {
		this.tokenProvider = tokenProvider;
		this.refreshTokenService = refreshTokenService;
		this.blacklistRedisTemplate = blacklistRedisTemplate;
	}

	public void refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = extractRefreshTokenFromCookie(request);

		if (refreshToken == null || !tokenProvider.validateRefreshToken(refreshToken)) {
			throw new NoSuchElementException("유효하지 않은 리프레시 토큰입니다.");
		}

		Claims claims = tokenProvider.getRefreshTokenClaims(refreshToken);

		Long userId = Long.valueOf(claims.get("adminId").toString());
		String email = claims.getSubject();
		String authorityString = claims.get("auth", String.class);

		Collection<? extends GrantedAuthority> authorities = Arrays.stream(authorityString.split(","))
				.map(SimpleGrantedAuthority::new)
				.toList();

		AdminDetails adminDetails = new AdminDetails(userId, email, null, authorities);
		Authentication authentication = new UsernamePasswordAuthenticationToken(adminDetails, null, authorities);

		// AccessToken & RefreshToken 재발급
		String newAccessToken = tokenProvider.createAccessToken(authentication);
		String newRefreshToken = tokenProvider.createRefreshToken(authentication);

		refreshTokenService.deleteRefreshToken(userId);
		refreshTokenService.saveRefreshToken(userId, newRefreshToken);

		addRefreshTokenCookie(response, newRefreshToken);
		response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken);
	}

	public void addAccessTokenToBlacklist(String accessToken) {
		long remainingTime = tokenProvider.getRemainingValidity(accessToken);

		blacklistRedisTemplate.opsForValue().set("blacklist:" + accessToken, 1L, Duration.ofSeconds(remainingTime));
	}
}
