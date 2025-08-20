package com.bankingwallet.auth.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Configuration
public class PasswordGrantConfiguration {

	@Bean
	public AuthenticationFilter passwordGrantFilter(AuthenticationManager authenticationManager,
	                                               RegisteredClientRepository clients,
	                                               JwtEncoder jwtEncoder,
	                                               OAuth2AuthorizationService authorizationService) {
		AuthenticationConverter converter = request -> {
			String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
			if (!"password".equals(grantType)) {
				return null;
			}
			String clientId = request.getParameter(OAuth2ParameterNames.CLIENT_ID);
			String username = request.getParameter(OAuth2ParameterNames.USERNAME);
			String password = request.getParameter(OAuth2ParameterNames.PASSWORD);
			if (clientId == null || username == null || password == null) {
				throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST));
			}
			RegisteredClient client = clients.findByClientId(clientId);
			if (client == null || !client.getAuthorizationGrantTypes().contains(new AuthorizationGrantType("password"))) {
				throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT));
			}
			return org.springframework.security.authentication.UsernamePasswordAuthenticationToken.unauthenticated(username, password);
		};

		AuthenticationFilter filter = new AuthenticationFilter(authenticationManager, converter) {
			@Override
			protected boolean shouldNotFilter(HttpServletRequest request) {
				return !("/oauth2/password".equals(request.getRequestURI()) &&
						"password".equals(request.getParameter(OAuth2ParameterNames.GRANT_TYPE)) &&
						"POST".equalsIgnoreCase(request.getMethod()));
			}
		};
		filter.setRequestMatcher(new AntPathRequestMatcher("/oauth2/password", "POST"));
		filter.setSuccessHandler((request, response, authentication) -> {
			Instant now = Instant.now();
			String issuer = request.getScheme() + "://" + request.getServerName() + (request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : (":" + request.getServerPort()));
			JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(issuer)
				.issuedAt(now)
				.expiresAt(now.plus(1, ChronoUnit.HOURS))
				.subject(authentication.getName())
				.claim("scope", "read write")
				.claim("roles", authentication.getAuthorities())
				.build();
			String tokenValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
			response.setContentType("application/json");
			response.getWriter().write("{\"access_token\":\"" + tokenValue + "\",\"token_type\":\"Bearer\"}");
		});
		filter.setFailureHandler((request, response, exception) -> {
			response.setStatus(400);
			response.setContentType("application/json");
			response.getWriter().write("{\"error\":\"invalid_grant\"}");
		});
		return filter;
	}
}
