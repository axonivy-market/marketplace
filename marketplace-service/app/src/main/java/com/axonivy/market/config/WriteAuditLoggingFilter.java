package com.axonivy.market.config;

import com.axonivy.market.model.UserInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Log4j2
@Component
public class WriteAuditLoggingFilter extends OncePerRequestFilter {
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String method = request.getMethod();
    return "GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method);
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      filterChain.doFilter(request, response);
    } finally {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String userId = "anonymous";
      String username = "anonymous";
      if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)
          && authentication.getPrincipal() instanceof UserInfo currentUser) {
        userId = currentUser.getId();
        username = currentUser.getUsername();
      }

      log.info("Audit method={} path={} status={} userId={} username={}",
          request.getMethod(), request.getRequestURI(), response.getStatus(), userId, username);
    }
  }
}
