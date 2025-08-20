package com.bankingwallet.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;

@Configuration
public class IdempotencyConfig {
	@Bean
	public HandlerInterceptor idempotencyInterceptor(StringRedisTemplate redisTemplate) {
		return new HandlerInterceptor() {
			@Override
			public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
				if ("POST".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().startsWith("/payments")) {
					String key = request.getHeader("Idempotency-Key");
					if (key != null && !key.isBlank()) {
						Boolean ok = redisTemplate.opsForValue().setIfAbsent("idem:" + key, "1", Duration.ofHours(24));
						if (Boolean.FALSE.equals(ok)) {
							response.setStatus(409);
							response.setContentType("application/json");
							response.getWriter().write("{\"error\":\"idempotency_conflict\"}");
							return false;
						}
					}
				}
				return true;
			}
		};
	}
}


