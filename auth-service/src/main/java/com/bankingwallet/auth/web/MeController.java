package com.bankingwallet.auth.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MeController {

	@GetMapping("/me")
	public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
		return jwt.getClaims();
	}
}
