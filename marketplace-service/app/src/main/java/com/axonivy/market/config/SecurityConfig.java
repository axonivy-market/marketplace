package com.axonivy.market.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

import static com.axonivy.market.constants.RequestMappingConstants.*;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
  private static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
  private static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";
  private static final String RELEASE_LETTER_MANAGEMENT = RELEASE_LETTER + "/management";
  private static final String[] AUTHENTICATED_GET_ENDPOINTS = {
      ADMIN_AUTH_V2 + SESSION,
      FEEDBACK,
      FEEDBACK + FEEDBACK_APPROVAL,
      LOGS,
      LOGS + DOWNLOAD_LOG_ARTIFACT,
      LOGS + LOG_STREAM,
      LOGS + LOG_STREAM_BY_TASK_KEY,
      MONITOR_DASHBOARD + REPOS,
      MONITOR_DASHBOARD + REPOS_REPORT,
      PRODUCT_MARKETPLACE_DATA + CUSTOM_SORT,
      RELEASE_LETTER_MANAGEMENT,
      RELEASE_LETTER + DRAFT_BY_ID,
      SECURITY_MONITOR,
      SYNC_TASK_EXECUTION,
      SYNC_TASK_EXECUTION + "/{jobKey}"
  };
  private static final String[] PUBLIC_POST_ENDPOINTS = {
      AUTH + GITHUB_LOGIN,
      AUTH + GITHUB_REQUEST_ACCESS,
      RELEASE_PREVIEW,
      ADMIN_AUTH_V2 + GITHUB_CALLBACK,
      ADMIN_AUTH_V2 + PASSKEY + AUTHENTICATE + OPTIONS,
      ADMIN_AUTH_V2 + PASSKEY + AUTHENTICATE + COMPLETE
  };
  private static final String[] PUBLIC_PUT_ENDPOINTS = {
      AUTH + GITHUB_VALIDATE_TOKEN
  };
  private final WriteAuditLoggingFilter writeAuditLoggingFilter;
  @Value("${server.servlet.session.cookie.name:ADMIN_SESSION}")
  private String sessionCookieName;

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
            .ignoringRequestMatchers(ADMIN_AUTH_V2 + GITHUB_CALLBACK))
        .securityContext(securityContext -> securityContext
            .requireExplicitSave(true)
            .securityContextRepository(securityContextRepository()))
        .sessionManagement(session -> session
            .sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::changeSessionId))
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers(HttpMethod.HEAD, "/**").permitAll()
            .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS).permitAll()
            .requestMatchers(HttpMethod.PUT, PUBLIC_PUT_ENDPOINTS).permitAll()
            .requestMatchers(HttpMethod.GET, AUTHENTICATED_GET_ENDPOINTS).authenticated()
            .requestMatchers(HttpMethod.POST, "/**").authenticated()
            .requestMatchers(HttpMethod.PUT, "/**").authenticated()
            .requestMatchers(HttpMethod.DELETE, "/**").authenticated()
            .anyRequest().permitAll())
        .logout(logout -> logout
            .logoutUrl(ADMIN_AUTH_V2 + LOGOUT)
            .invalidateHttpSession(true)
            .deleteCookies(sessionCookieName)
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
    private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
        Supplier<CsrfToken> csrfToken) {
      this.xor.handle(request, response, csrfToken);
      csrfToken.get();
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
      String headerValue = request.getHeader(csrfToken.getHeaderName());
      if (StringUtils.hasText(headerValue)) {
        return this.plain.resolveCsrfTokenValue(request, csrfToken);
      }
      return this.xor.resolveCsrfTokenValue(request, csrfToken);
    }
  }
}
