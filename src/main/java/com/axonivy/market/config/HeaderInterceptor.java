package com.axonivy.market.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.axonivy.market.exceptions.MissingHeaderException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class HeaderInterceptor implements HandlerInterceptor {

  @Value("${request.header}")
  private String requestHeader;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    if (!requestHeader.equals(request.getHeader("X-Requested-By"))) {
      throw new MissingHeaderException();
    }
    return true;
  }
}
