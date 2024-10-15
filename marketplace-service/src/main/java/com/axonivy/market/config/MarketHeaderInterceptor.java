package com.axonivy.market.config;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.exceptions.model.MissingHeaderException;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class MarketHeaderInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    if (HttpMethod.OPTIONS.name().equalsIgnoreCase(request.getMethod())) {
      return true;
    }
    if (!HttpMethod.GET.name().equalsIgnoreCase(request.getMethod())
        && StringUtils.isBlank(request.getHeader(CommonConstants.REQUESTED_BY))) {
      throw new MissingHeaderException();
    }
    return true;
  }
}