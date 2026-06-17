package com.axonivy.market.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import static com.axonivy.market.core.constants.CoreRequestMappingConstants.IMAGE;
import static com.axonivy.market.core.constants.CoreRequestMappingConstants.ROOT;
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
  private static final String[] AUTHENTICATED_POST_ENDPOINTS = {
      ADMIN_AUTH_V2 + PASSKEY + REGISTER + OPTIONS,
      ADMIN_AUTH_V2 + PASSKEY + REGISTER + COMPLETE,
      FEEDBACK,
      MONITOR_DASHBOARD + SYNC,
      MONITOR_DASHBOARD + SYNC_ONE_PRODUCT_BY_ID,
      MONITOR_DASHBOARD + FOCUSED,
      PRODUCT + SYNC,
      PRODUCT + SYNC_ONE_PRODUCT_BY_ID,
      PRODUCT + SYNC_FIRST_PUBLISHED_DATE_ALL_PRODUCTS,
      PRODUCT + SYNC_ZIP_ARTIFACTS,
      PRODUCT_DETAILS + SYNC_RELEASE_NOTES_FOR_PRODUCTS,
      PRODUCT_MARKETPLACE_DATA + CUSTOM_SORT,
      RELEASE_LETTER,
      RELEASE_LETTER + SAVE_AS_DRAFT,
      SECURITY_MONITOR,
      EXTERNAL_DOCUMENT + SYNC
  };
  private static final String[] AUTHENTICATED_PUT_ENDPOINTS = {
      PRODUCT_MARKETPLACE_DATA + DEPRECATION_BY_ID
  };
  private static final String[] AUTHENTICATED_DELETE_ENDPOINTS = {
      RELEASE_LETTER + BY_ID
  };
  private final WriteAuditLoggingFilter writeAuditLoggingFilter;
  @Value("${server.servlet.session.cookie.name:ADMIN_SESSION}")
  private String sessionCookieName;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
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
            .ignoringRequestMatchers(ADMIN_AUTH_V2 + GITHUB_CALLBACK))
        .securityContext(securityContext -> securityContext
            .requireExplicitSave(true)
            .securityContextRepository(securityContextRepository()))
        .sessionManagement(session -> session.sessionFixation(sessionFixation -> sessionFixation.changeSessionId()))
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers(HttpMethod.GET, AUTHENTICATED_GET_ENDPOINTS).authenticated()
            .requestMatchers(HttpMethod.POST, AUTHENTICATED_POST_ENDPOINTS).authenticated()
            .requestMatchers(HttpMethod.PUT, AUTHENTICATED_PUT_ENDPOINTS).authenticated()
            .requestMatchers(HttpMethod.DELETE, AUTHENTICATED_DELETE_ENDPOINTS).authenticated()
            .requestMatchers(HttpMethod.PUT, ADMIN_AUTH_V2 + GITHUB_VALIDATE_TOKEN).permitAll()
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
}
