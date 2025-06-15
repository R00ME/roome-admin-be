package com.roome.admin.roomeadminbe.global.security.jwt.provider;

import com.roome.admin.roomeadminbe.global.security.model.AdminDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TokenProvider implements InitializingBean {

	private static final String AUTHORITIES_KEY = "auth";
	private final Logger logger = LoggerFactory.getLogger(TokenProvider.class);
	private final String accessSecret;
	private final long accessTokenValidityInMillis;
	private final String refreshSecret;
	private final long refreshTokenValidityInMillis;
	private SecretKey accessKey;
	private SecretKey refreshKey;

	public TokenProvider(
			@Value("${jwt.access-token.secret}") String accessSecret,
			@Value("${jwt.access-token.validity-in-seconds}") long accessValidity,
			@Value("${jwt.refresh-token.secret}") String refreshSecret,
			@Value("${jwt.refresh-token.validity-in-seconds}") long refreshValidity
	) {
		this.accessSecret = accessSecret;
		this.refreshSecret = refreshSecret;
		this.accessTokenValidityInMillis = accessValidity * 1000;
		this.refreshTokenValidityInMillis = refreshValidity * 1000;
	}

	// 빈이 생성되고 주입을 받은 후에 secret값을 Base64 Decode해서 key 변수에 할당하기 위해
	@Override
	public void afterPropertiesSet() {
		this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
		this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret));
	}

	public String createAccessToken(Authentication authentication) {
		AdminDetails principal = (AdminDetails) authentication.getPrincipal();

		String authorities = authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(","));

		long now = System.currentTimeMillis();

		return Jwts.builder()
				.setSubject(principal.getAdminId().toString())
				.claim("email", principal.getUsername())
				.claim("auth", authorities)
				.setIssuedAt(new Date(now))
				.setExpiration(new Date(now + accessTokenValidityInMillis))
				.signWith(accessKey, SignatureAlgorithm.HS256)
				.compact();
	}

	public String createRefreshToken(Authentication authentication) {
		AdminDetails principal = (AdminDetails) authentication.getPrincipal();

		Long userId = principal.getAdminId();
		String email = principal.getUsername();

		long now = System.currentTimeMillis();

		return Jwts.builder()
				.setSubject(userId.toString())
				.claim("email", email)
				.setIssuedAt(new Date(now))
				.setExpiration(new Date(now + refreshTokenValidityInMillis))
				.signWith(refreshKey, SignatureAlgorithm.HS256)
				.compact();
	}

	// 토큰으로 클레임을 만들고 이를 이용해 유저 객체를 만들어서 최종적으로 authentication 객체를 리턴
	public Authentication getAuthenticationFromAccessToken(String token) {
		Claims claims = Jwts.parserBuilder()
				.setSigningKey(accessKey)
				.build()
				.parseClaimsJws(token)
				.getBody();

		Collection<? extends GrantedAuthority> authorities =
				Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
						.map(SimpleGrantedAuthority::new)
						.collect(Collectors.toList());

		User principal = new User(claims.getSubject(), "", authorities);

		return new UsernamePasswordAuthenticationToken(principal, token, authorities);
	}

	public Authentication getAuthenticationFromUserId(Long userId) {
		// 실제로는 DB 또는 UserDetailsService에서 조회
		UserDetails userDetails = new User(userId.toString(), "", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
		return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
	}

	public Long getUserIdFromAccessToken(String accessToken) {
		Claims claims = getAccessTokenClaims(accessToken);
		return Long.valueOf(claims.getSubject()); // subject에 userId가 저장되어 있음
	}

	public Long getUserFromRefreshToken(String refreshToken) {
		Claims claims = getRefreshTokenClaims(refreshToken);
		return Long.valueOf(claims.getSubject());
	}

	public Claims getAccessTokenClaims(String token) {
		return parseClaims(token, accessKey);
	}

	public Claims getRefreshTokenClaims(String token) {
		return parseClaims(token, refreshKey);
	}

	private Claims parseClaims(String token, SecretKey key) {
		return Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	public String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}

	// 토큰의 유효성 검증을 수행
	public boolean validateAccessToken(String token) {
		return validateToken(token, accessKey);
	}

	public boolean validateRefreshToken(String token) {
		return validateToken(token, refreshKey);
	}

	private boolean validateToken(String token, SecretKey key) {
		try {
			Jwts.parserBuilder()
					.setSigningKey(key)
					.build()
					.parseClaimsJws(token);
			return true;
		} catch (SecurityException | MalformedJwtException e) {
			log.warn("잘못된 JWT 서명입니다: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			log.warn("만료된 JWT 토큰입니다: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			log.warn("지원하지 않는 JWT 토큰입니다: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			log.warn("JWT claims가 비어있습니다: {}", e.getMessage());
		}
		return false;
	}
}
