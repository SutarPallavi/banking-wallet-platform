package com.bankingwallet.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.RetryGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Configuration
public class GatewayConfig {

	private static final Logger log = LoggerFactory.getLogger(GatewayConfig.class);

	@Value("${security.oauth2.resourceserver.jwt.issuer-uri:http://auth-service:9000}")
	private String issuerUri;

	@Value("${security.oauth2.resourceserver.jwt.jwk-set-uri:}")
	private String jwkSetUri;

	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		http
			.csrf(ServerHttpSecurity.CsrfSpec::disable)
			.authorizeExchange(exchanges -> exchanges
				.pathMatchers("/actuator/**", "/docs", "/swagger-ui/**", "/v3/api-docs/**", "/test/**", "/auth/token").permitAll()
				.anyExchange().authenticated()
			)
			.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {
				if (StringUtils.hasText(jwkSetUri)) {
					jwt.jwkSetUri(jwkSetUri);
				} else {
					jwt.jwtDecoder(ReactiveJwtDecoders.fromIssuerLocation(issuerUri));
				}
			}));
		return http.build();
	}

	@Bean
	public RouteLocator routes(RouteLocatorBuilder builder) {
		RedisRateLimiter rateLimiter = new RedisRateLimiter(100, 200);
		return builder.routes()
			.route("auth-token", r -> r
				.path("/auth/token")
				.filters(f -> f.rewritePath("/auth/token", "/oauth2/token"))
				.uri("http://auth-service:9000"))
			.route("accounts", r -> r.path("/api/accounts/**").uri("lb://account-service"))
			.route("transactions", r -> r.path("/api/transactions/**").uri("lb://transaction-service"))
			.route("payments", r -> r.path("/api/payments/**").uri("lb://payment-service"))
			.route("notifications", r -> r.path("/api/notifications/**").uri("lb://notification-service"))
			.build();
	}

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public GlobalFilter correlationIdFilter() {
		return (exchange, chain) -> {
			String incoming = exchange.getRequest().getHeaders().getFirst("X-Correlation-Id");
			String correlationId = StringUtils.hasText(incoming) ? incoming : UUID.randomUUID().toString();
			return chain.filter(exchange.mutate().request(builder -> builder.headers(httpHeaders -> httpHeaders.set("X-Correlation-Id", correlationId))).build())
				.then(Mono.fromRunnable(() -> exchange.getResponse().getHeaders().set("X-Correlation-Id", correlationId)));
		};
	}

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE + 1)
	public GlobalFilter roleBasedRateLimiter(ReactiveStringRedisTemplate redisTemplate) {
		// Enforce 100 rpm for USER, 1000 rpm for ADMIN, keyed by principal and current minute bucket
		return (exchange, chain) -> ReactiveSecurityContextHolder.getContext()
			.map(ctx -> ctx.getAuthentication())
			.flatMap(auth -> {
				String principal = auth.getName();
				boolean isAdmin = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(a -> a.equals("ROLE_ADMIN"));
				int limit = isAdmin ? 1000 : 100;
				String window = DateTimeFormatter.ofPattern("yyyyMMddHHmm").withZone(ZoneOffset.UTC).format(Instant.now());
				String key = "rate:" + (isAdmin ? "ADMIN" : "USER") + ":" + principal + ":" + window;
				return redisTemplate.opsForValue().increment(key)
					.flatMap(val -> {
						if (val == 1L) {
							return redisTemplate.expire(key, Duration.ofMinutes(1)).thenReturn(val);
						}
						return Mono.just(val);
					})
					.flatMap(val -> {
						if (val != null && val > limit) {
							exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
							exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(limit));
							exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", "0");
							return exchange.getResponse().setComplete();
						}
						exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(limit));
						exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(Math.max(0, limit - (val == null ? 0 : val.intValue()))));
						return chain.filter(exchange);
					});
			})
			.switchIfEmpty(chain.filter(exchange));
	}

	@Bean
	public GlobalFilter loggingFilter() {
		return (exchange, chain) -> {
			String path = exchange.getRequest().getURI().getPath();
			String method = exchange.getRequest().getMethod() != null ? exchange.getRequest().getMethod().name() : "";
			String auth = mask(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
			log.info("REQ {} {} auth={} corr={}", method, path, auth, exchange.getRequest().getHeaders().getFirst("X-Correlation-Id"));
			return chain.filter(exchange).doOnSuccess(aVoid -> {
				int status = exchange.getResponse().getRawStatusCode();
				log.info("RES {} {} -> {} corr={}", method, path, status, exchange.getResponse().getHeaders().getFirst("X-Correlation-Id"));
			});
		};
	}

	private String mask(String value) {
		if (!StringUtils.hasText(value)) return "";
		int keep = Math.min(6, value.length());
		return value.substring(0, keep) + "***";
	}
}
