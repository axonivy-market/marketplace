package com.axonivy.market.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.exceptions.model.MissingHeaderException;

import io.swagger.v3.oas.models.PathItem.HttpMethod;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class MarketHeaderInterceptor implements HandlerInterceptor {

  @Value("${request.header}")
  private String requestHeader;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    if (HttpMethod.OPTIONS.name().equalsIgnoreCase(request.getMethod())) {
      return true;
    }
    if (!requestHeader.equals(request.getHeader(CommonConstants.REQUESTED_BY))) {
      throw new MissingHeaderException();
    }
    return true;
  }
}