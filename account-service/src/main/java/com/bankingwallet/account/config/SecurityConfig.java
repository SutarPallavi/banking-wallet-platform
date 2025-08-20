package com.bankingwallet.account.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
				.anyRequest().authenticated()
			)
			.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
		return http.build();
	}
}

