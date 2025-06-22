package com.roome.admin.roomeadminbe.global.config;

import com.roome.admin.roomeadminbe.global.security.jwt.filter.JwtFilter;
import com.roome.admin.roomeadminbe.global.security.jwt.provider.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
public class JwtSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private final TokenProvider tokenProvider;
    @Qualifier("blacklistRedisTemplate")
    private final RedisTemplate<String, Long> blacklistRedisTemplate;

    @Override
    public void configure(HttpSecurity http) {

        http.addFilterBefore(
                new JwtFilter(tokenProvider, blacklistRedisTemplate),
                UsernamePasswordAuthenticationFilter.class
        );
    }
}
