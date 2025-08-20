package com.bankingwallet.payment.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	private final org.springframework.web.servlet.HandlerInterceptor idempotencyInterceptor;

	public WebConfig(org.springframework.web.servlet.HandlerInterceptor idempotencyInterceptor) {
		this.idempotencyInterceptor = idempotencyInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(idempotencyInterceptor).addPathPatterns("/payments/**");
	}
}


