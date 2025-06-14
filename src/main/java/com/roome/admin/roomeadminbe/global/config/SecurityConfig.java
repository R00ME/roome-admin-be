package com.roome.admin.roomeadminbe.global.config;

import com.roome.admin.roomeadminbe.global.security.jwt.handler.JwtAccessDeniedHandler;
import com.roome.admin.roomeadminbe.global.security.jwt.handler.JwtAuthenticationEntryPoint;
import com.roome.admin.roomeadminbe.global.security.jwt.provider.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final TokenProvider tokenProvider;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
		JwtSecurityConfig jwtSecurityConfig = new JwtSecurityConfig(tokenProvider);

		httpSecurity
				.csrf(csrf -> csrf.disable())
				.exceptionHandling(exceptionHandling ->
						exceptionHandling
								.authenticationEntryPoint(jwtAuthenticationEntryPoint)
								.accessDeniedHandler(jwtAccessDeniedHandler)
				)
				.headers(headers ->
						headers
								.frameOptions(frameOptions -> frameOptions.sameOrigin())
				)
				.sessionManagement(sessionManagement ->
						sessionManagement
								.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				)
				.authorizeHttpRequests(authorizeRequests ->
						authorizeRequests
								.requestMatchers("/api/admin/super/invite").permitAll()
								.requestMatchers("/roome/bo/admin/common/auth/password").permitAll()
								.requestMatchers("/roome/bo/admin/common/auth/login").permitAll()
								.requestMatchers(PathRequest.toH2Console()).permitAll()
								.requestMatchers("/favicon.ico").permitAll()
								.anyRequest().authenticated()
				);

		jwtSecurityConfig.configure(httpSecurity); // JwtSecurityConfig를 적용

		return httpSecurity.build();
	}
}
