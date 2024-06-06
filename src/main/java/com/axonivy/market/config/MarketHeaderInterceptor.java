package com.axonivy.market.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.axonivy.market.exceptions.MissingHeaderException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import static com.axonivy.market.constants.CommonConstants.*;

@Component
public class MarketHeaderInterceptor implements HandlerInterceptor {

  @Value("${request.header}")
  private String requestHeader;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    if (!requestHeader.equals(request.getHeader(REQUESTED_BY))) {
      throw new MissingHeaderException();
    }
    return true;
  }
}
