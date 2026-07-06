package com.axonivy.market.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import java.util.function.Supplier;

import static com.axonivy.market.constants.RequestMappingConstants.*;
import static com.axonivy.market.core.constants.CoreRequestMappingConstants.API;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
  private static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
  private static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";
  private static final String[] AUTHENTICATED_GET_ENDPOINTS = {
        API + INTERNAL +"/**",
        FEEDBACK + FEEDBACK_APPROVAL,
        PRODUCT_MARKETPLACE_DATA+"/**",
        RELEASE_LETTER + "/**",
  };
  private static final String[] PUBLIC_GET_ENDPOINTS = {
      PRODUCT_MARKETPLACE_DATA + "/installation-count/**"
  };
  private static final String[] PUBLIC_POST_ENDPOINTS = {
      ADMIN_AUTH + GITHUB_CALLBACK
  };
  private static final String[] PUBLIC_PUT_ENDPOINTS = {
      AUTH + GITHUB_VALIDATE_TOKEN
  };
  private final WriteAuditLoggingFilter writeAuditLoggingFilter;
  private final SessionCookieProperties sessionCookieProperties;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
    csrfTokenRepository.setCookieName(CSRF_COOKIE_NAME);
    csrfTokenRepository.setHeaderName(CSRF_HEADER_NAME);
    csrfTokenRepository.setCookiePath("/");

    http
        .cors(Customizer.withDefaults())
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .csrf(csrf -> csrf
            .csrfTokenRepository(csrfTokenRepository)
            .csrfTokenRequestHandler(spaCsrfTokenRequestHandler())
            .ignoringRequestMatchers(PUBLIC_POST_ENDPOINTS))
        .securityContext(securityContext -> securityContext
            .requireExplicitSave(true)
            .securityContextRepository(securityContextRepository()))
        .sessionManagement(session -> session
            .sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::changeSessionId))
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS).permitAll()
            .requestMatchers(HttpMethod.PUT, PUBLIC_PUT_ENDPOINTS).permitAll()
            .requestMatchers(HttpMethod.GET, AUTHENTICATED_GET_ENDPOINTS).authenticated()
            .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS).permitAll()
            .requestMatchers(HttpMethod.POST, "/**").authenticated()
            .requestMatchers(HttpMethod.PUT, "/**").authenticated()
            .requestMatchers(HttpMethod.DELETE, "/**").authenticated()
            .anyRequest().permitAll())
        .logout(logout -> logout
            .logoutUrl(ADMIN_AUTH + "/logout")
            .invalidateHttpSession(true)
            .deleteCookies(sessionCookieProperties.getName())
            .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()))
        .exceptionHandling(exceptionHandling -> exceptionHandling
            .authenticationEntryPoint(authenticationEntryPoint())
            .accessDeniedHandler((request, response, accessDeniedException) -> {
              Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
              boolean anonymous = authentication == null || authentication instanceof AnonymousAuthenticationToken;
              int status = anonymous ? HttpServletResponse.SC_UNAUTHORIZED : HttpServletResponse.SC_FORBIDDEN;
              response.sendError(status);
            }));

    http.addFilterAfter(writeAuditLoggingFilter, SecurityContextHolderFilter.class);

    return http.build();
  }

  @Bean
  public SessionAuthenticationStrategy sessionAuthenticationStrategy() {
    return new ChangeSessionIdAuthenticationStrategy();
  }

  @Bean
  public SecurityContextRepository securityContextRepository() {
    return new HttpSessionSecurityContextRepository();
  }

  @Bean
  public AuthenticationEntryPoint authenticationEntryPoint() {
    return new HttpStatusEntryPoint(org.springframework.http.HttpStatus.UNAUTHORIZED);
  }

  @Bean
  public CsrfTokenRequestHandler spaCsrfTokenRequestHandler() {
    return new SpaCsrfTokenRequestHandler();
  }

  private static final class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {
    private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
        Supplier<CsrfToken> csrfToken) {
      this.plain.handle(request, response, csrfToken);
      csrfToken.get();
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
      return request.getHeader(csrfToken.getHeaderName());
    }
  }
}
