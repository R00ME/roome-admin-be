package com.roome.admin.roomeadminbe.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@EnableCaching
@Slf4j
@Configuration
public class RedisConfig {

	@Value("${spring.data.redis.host}")
	private String host;

	@Value("${spring.data.redis.port}")
	private int port;

	@Value("${spring.data.redis.password}")
	private String password;

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
		config.setHostName(host);
		config.setPort(port);
		config.setPassword(password);

		LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
		factory.afterPropertiesSet(); // 강제 초기화

		// Redis 연결 확인 (Ping 테스트)
		try {
			String result = factory.getConnection().ping();
			log.info("Redis 연결 성공! 응답: {}", result);
		} catch (Exception e) {
			log.error("Redis 연결 실패: {}", e.getMessage(), e);
		}

		return factory;
	}

	@Bean(name = "refreshTokenRedisTemplate")
	public RedisTemplate<String, String> refreshTokenRedisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, String> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		// key/value 직렬화 방식
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new StringRedisSerializer());

		return template;
	}

	@Bean
	public RedisTemplate<String, Long> blacklistRedisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Long> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new GenericToStringSerializer<>(Long.class));
		return template;
	}
}
