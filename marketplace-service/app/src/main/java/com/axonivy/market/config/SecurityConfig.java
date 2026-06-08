package com.axonivy.market.config;

import com.axonivy.market.constants.RequestParamConstants;
import com.axonivy.market.security.SecurityAuthorities;
import com.axonivy.market.security.AuthenticatedUser;
import com.axonivy.market.security.SecurityJwtProperties;
import com.axonivy.market.service.impl.JwtServiceImpl;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder.SecretKeyJwtDecoderBuilder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityJwtProperties.class)
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      BearerTokenResolver bearerTokenResolver,
      Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
        .oauth2ResourceServer(oauth2 -> oauth2
            .bearerTokenResolver(bearerTokenResolver)
            .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
        .cors(Customizer.withDefaults());

    return http.build();
  }

  @Bean
  public SecretKey securityJwtSigningKey(SecurityJwtProperties securityJwtProperties) {
    return new SecretKeySpec(
        securityJwtProperties.getSecret().getBytes(StandardCharsets.UTF_8),
        "HmacSHA256");
  }

  @Bean
  public JwtEncoder jwtEncoder(SecretKey securityJwtSigningKey) {
    return new NimbusJwtEncoder(new ImmutableSecret<>(securityJwtSigningKey));
  }

  @Bean
  public JwtDecoder jwtDecoder(SecretKey securityJwtSigningKey, SecurityJwtProperties securityJwtProperties) {
    SecretKeyJwtDecoderBuilder builder = NimbusJwtDecoder.withSecretKey(securityJwtSigningKey)
        .macAlgorithm(MacAlgorithm.HS256);
    var decoder = builder.build();
    decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
        JwtValidators.createDefaultWithIssuer(securityJwtProperties.getIssuer()),
        new JwtTimestampValidator(),
        new JwtClaimValidator<List<String>>("aud",
            audience -> audience != null && audience.contains(securityJwtProperties.getAudience())),
        new JwtClaimValidator<Boolean>(JwtServiceImpl.ADMIN_CLAIM, Boolean.TRUE::equals),
        new JwtClaimValidator<List<String>>(JwtServiceImpl.AUTHORITIES_CLAIM,
            authorities -> authorities != null && authorities.contains(SecurityAuthorities.MARKET_ADMIN)),
        new JwtClaimValidator<String>("sub", subject -> subject != null && !subject.isBlank()),
        new JwtClaimValidator<String>(JwtServiceImpl.USERNAME_CLAIM, username -> username != null && !username.isBlank())));
    return decoder;
  }

  @Bean
  public BearerTokenResolver bearerTokenResolver() {
    DefaultBearerTokenResolver defaultBearerTokenResolver = new DefaultBearerTokenResolver();
    return request -> {
      String xAuthorization = request.getHeader(RequestParamConstants.X_AUTHORIZATION);
      String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

      if (xAuthorization != null && !xAuthorization.isBlank() && authorization != null && !authorization.isBlank()) {
        return null;
      }

      if (xAuthorization != null && !xAuthorization.isBlank()) {
        return xAuthorization.startsWith("Bearer ") ? xAuthorization.substring(7).trim() : xAuthorization.trim();
      }

      if (authorization != null && !authorization.isBlank()) {
        return defaultBearerTokenResolver.resolve(request);
      }

      return null;
    };
  }

  @Bean
  public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
    return jwt -> {
      List<SimpleGrantedAuthority> authorities = Optional.ofNullable(jwt.getClaimAsStringList(
              JwtServiceImpl.AUTHORITIES_CLAIM))
          .orElse(List.of())
          .stream()
          .map(SimpleGrantedAuthority::new)
          .toList();

      var principal = new AuthenticatedUser(
          jwt.getSubject(),
          jwt.getClaimAsString(JwtServiceImpl.USERNAME_CLAIM),
          jwt.getClaimAsString(com.axonivy.market.constants.GitHubConstants.NAME),
          jwt.getClaimAsString(JwtServiceImpl.AVATAR_URL_CLAIM),
          jwt.getClaimAsString(JwtServiceImpl.PROVIDER_CLAIM),
          Boolean.TRUE.equals(jwt.getClaim(JwtServiceImpl.ADMIN_CLAIM)));

      return UsernamePasswordAuthenticationToken.authenticated(principal, jwt, authorities);
    };
  }
}
