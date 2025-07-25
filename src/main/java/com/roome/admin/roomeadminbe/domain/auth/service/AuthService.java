package com.roome.admin.roomeadminbe.domain.auth.service;

import com.roome.admin.roomeadminbe.domain.auth.dto.response.TokenResponse;
import com.roome.admin.roomeadminbe.domain.auth.dto.request.LoginRequest;
import com.roome.admin.roomeadminbe.global.security.jwt.provider.TokenProvider;
import com.roome.admin.roomeadminbe.global.security.jwt.service.RefreshTokenService;
import com.roome.admin.roomeadminbe.global.security.jwt.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.NoSuchElementException;

import static com.roome.admin.roomeadminbe.global.security.util.CookieUtil.deleteRefreshTokenCookie;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final RefreshTokenService refreshTokenService;
    private final TokenService tokenService;

    public TokenResponse login(LoginRequest loginRequestDto) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(), loginRequestDto.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.createAccessToken(authentication);
        String refreshToken = tokenProvider.createRefreshToken(authentication);

        return new TokenResponse(accessToken, refreshToken);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. AccessToken 추출
        String accessToken = tokenProvider.resolveToken(request);
        if (accessToken == null || !tokenProvider.validateAccessToken(accessToken)) {
            throw new NoSuchElementException("유효하지 않은 AccessToken입니다.");
        }

        // 2. 사용자 정보 추출
        Long userId = tokenProvider.getUserIdFromAccessToken(accessToken);

        // 3. accessToken blackList 에 추가
        tokenService.addAccessTokenToBlacklist(accessToken);
        // 4. Redis에서 RefreshToken 삭제
        refreshTokenService.deleteRefreshToken(userId);

        // 5. 쿠키에서 RefreshToken 제거 (Set-Cookie: Max-Age=0)
        deleteRefreshTokenCookie(response);
    }

    // 랜덤 비밀번호 생성 로직
    private String generateRandomPassword() {
        int length = 10;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int idx = random.nextInt(chars.length());
            sb.append(chars.charAt(idx));
        }
        return sb.toString();
    }
}
