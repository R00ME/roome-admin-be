package com.roome.admin.roomeadminbe.global.security.jwt.filter;

import com.roome.admin.roomeadminbe.global.security.jwt.provider.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends GenericFilterBean {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    private final TokenProvider tokenProvider;
    @Qualifier("BO_blacklistRedisTemplate")
    private final RedisTemplate<String, Long> blacklistRedisTemplate;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException, IOException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String jwt = tokenProvider.resolveToken(httpServletRequest);
        String requestURI = httpServletRequest.getRequestURI();

        if ("OPTIONS".equals(httpServletRequest.getMethod())) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (StringUtils.hasText(jwt) && tokenProvider.validateAccessToken(jwt)) {
            // blacklist 확인
            Boolean isBlacklisted = blacklistRedisTemplate.hasKey("blacklist:" + jwt);
            if (Boolean.TRUE.equals(isBlacklisted)) {
                HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
                // 블랙리스트 토큰: 인증 거부
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.getWriter().write("AccessToken is blacklisted.");
                return;
            }

            Authentication authentication = tokenProvider.getAuthenticationFromAccessToken(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("Security Context에 '{}' 인증 정보를 저장했습니다, uri: {}", authentication.getName(), requestURI);
        } else {
            logger.debug("유효한 JWT 토큰이 없습니다, uri: {}", requestURI);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
