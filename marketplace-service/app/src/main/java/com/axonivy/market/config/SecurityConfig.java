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
import static com.axonivy.market.constants.RequestMappingConstants.ADMIN_AUTH_V2;
import static com.axonivy.market.constants.RequestMappingConstants.AUTHENTICATE;
import static com.axonivy.market.constants.RequestMappingConstants.COMPLETE;
import static com.axonivy.market.constants.RequestMappingConstants.GITHUB_CALLBACK;
import static com.axonivy.market.constants.RequestMappingConstants.LOGOUT;
import static com.axonivy.market.constants.RequestMappingConstants.OPTIONS;
import static com.axonivy.market.constants.RequestMappingConstants.PASSKEY;

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
        .csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository))
        .securityContext(securityContext -> securityContext
            .requireExplicitSave(true)
            .securityContextRepository(securityContextRepository()))
        .sessionManagement(session -> session.sessionFixation(sessionFixation -> sessionFixation.changeSessionId()))
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/**").permitAll()
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
