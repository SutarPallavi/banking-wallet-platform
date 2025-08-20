package com.bankingwallet.auth.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2AccessTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {

	@Bean
	public SecurityFilterChain authServerSecurityFilterChain(HttpSecurity http) throws Exception {
		OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
		http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
			.oidc(Customizer.withDefaults());
		return http.build();
	}

	@Bean
	public SecurityFilterChain appSecurityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/.well-known/jwks.json", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
				.anyRequest().authenticated()
			)
			.oauth2ResourceServer(resource -> resource.jwt(Customizer.withDefaults()))
			.formLogin(Customizer.withDefaults());
		return http.build();
	}

	@Bean
	public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
		InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
		manager.createUser(User.withUsername("alice@example.com").password(passwordEncoder.encode("password")).roles("USER").build());
		manager.createUser(User.withUsername("admin@example.com").password(passwordEncoder.encode("password")).roles("ADMIN").build());
		return manager;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return new ProviderManager(provider);
	}

	@Bean
	public RegisteredClientRepository registeredClientRepository(PasswordEncoder passwordEncoder) {
		RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
			.clientId("demo-client")
			.clientSecret(passwordEncoder.encode("demo-secret"))
			.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
			.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
			.authorizationGrantType(new AuthorizationGrantType("password"))
			.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
			.scope("openid")
			.scope("profile")
			.scope("read")
			.scope("write")
			.build();
		return new InMemoryRegisteredClientRepository(registeredClient);
	}

	@Bean
	public OAuth2AuthorizationService authorizationService() {
		return new org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService();
	}

	@Bean
	public OAuth2TokenGenerator<?> tokenGenerator(JwtEncoder jwtEncoder) {
		JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
		OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
		DelegatingOAuth2TokenGenerator delegate = new DelegatingOAuth2TokenGenerator(accessTokenGenerator, jwtGenerator);
		return delegate;
	}

	@Bean
	public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
		return context -> {
			context.getJwsHeader().algorithm(org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256);
			context.getJwsHeader().header("kid", "rsa-key");
		};
	}

	@Bean
	public AuthorizationServerSettings authorizationServerSettings(org.springframework.core.env.Environment env) {
		String issuer = env.getProperty("spring.security.oauth2.authorizationserver.issuer", "http://auth-service:9000");
		return AuthorizationServerSettings.builder().issuer(issuer).build();
	}

	@Bean
	public KeyPair keyPair() throws Exception {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		return keyPairGenerator.generateKeyPair();
	}

	@Bean
	public JWKSource<SecurityContext> jwkSource(KeyPair keyPair) {
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		JWK jwk = new RSAKey.Builder(publicKey)
			.privateKey(privateKey)
			.keyID("rsa-key")
			.build();
		JWKSet jwkSet = new JWKSet(jwk);
		return new ImmutableJWKSet<>(jwkSet);
	}

	@Bean
	public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
		return new NimbusJwtEncoder(jwkSource);
	}

	@Bean
	public JwtDecoder jwtDecoder(KeyPair keyPair) {
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		return NimbusJwtDecoder.withPublicKey(publicKey).build();
	}
}
