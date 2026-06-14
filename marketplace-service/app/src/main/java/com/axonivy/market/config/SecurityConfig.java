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
  private final WriteAuditLoggingFilter writeAuditLoggingFilter;
  @Value("${SESSION_COOKIE_NAME:ADMIN_SESSION}")
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
            .requestMatchers(HttpMethod.GET,
                ROOT,
                IMAGE + BY_ID,
                PRODUCT,
                PRODUCT + IDS,
                PRODUCT + PRODUCT_RATING_BY_ID,
                PRODUCT_DETAILS + BY_ID_AND_VERSION,
                PRODUCT_DETAILS + BEST_MATCH_BY_ID_AND_VERSION,
                PRODUCT_DETAILS + BY_ID,
                PRODUCT_DETAILS + VERSIONS_BY_ID,
                PRODUCT_DETAILS + PRODUCT_JSON_CONTENT_BY_PRODUCT_ID_AND_VERSION,
                PRODUCT_DETAILS + PRODUCT_PUBLIC_RELEASES,
                PRODUCT_DETAILS + PRODUCT_PUBLIC_RELEASE_BY_RELEASE_ID,
                PRODUCT_DETAILS + LATEST_ARTIFACT_DOWNLOAD_URL_BY_ID,
                PRODUCT_DETAILS + ARTIFACTS_AS_ZIP,
                EXTERNAL_DOCUMENT + BY_ID_AND_VERSION,
                EXTERNAL_DOCUMENT + DOCUMENT_BEST_MATCH,
                DOCUMENT + DOCUMENT_VERSION_LANGUAGE,
                RELEASE_LETTER,
                RELEASE_LETTER + BY_ID,
                RELEASE_LETTER + BY_LATEST,
                FEEDBACK + PRODUCT_BY_ID,
                FEEDBACK + BY_ID,
                FEEDBACK,
                FEEDBACK + PRODUCT_RATING_BY_ID,
                PRODUCT_MARKETPLACE_DATA + CUSTOM_SORT,
                PRODUCT_MARKETPLACE_DATA + INSTALLATION_COUNT_BY_ID,
                PRODUCT_MARKETPLACE_DATA + DEPRECATIONS,
                PRODUCT_MARKETPLACE_DATA + VERSION_DOWNLOAD_BY_ID,
                PRODUCT_DESIGNER_INSTALLATION + DESIGNER_INSTALLATION_BY_ID,
                PRODUCT_DETAILS + PRODUCT_JSON_CONTENT_BY_PRODUCT_ID_AND_VERSION,
                ADMIN_AUTH_V2 + GITHUB_AUTHORIZATION,
                ADMIN_AUTH_V2 + CSRF)
                .permitAll()
            .requestMatchers(HttpMethod.POST, "/auth/github/login", "/auth/github/request-access").permitAll()
            .requestMatchers(HttpMethod.POST, ADMIN_AUTH_V2 + GITHUB_CALLBACK).permitAll()
            .requestMatchers(HttpMethod.POST,
                ADMIN_AUTH_V2 + PASSKEY + AUTHENTICATE + OPTIONS,
                ADMIN_AUTH_V2 + PASSKEY + AUTHENTICATE + COMPLETE).permitAll()
            .anyRequest().authenticated())
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
