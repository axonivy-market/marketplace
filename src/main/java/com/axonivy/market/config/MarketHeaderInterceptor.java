package com.axonivy.market.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.axonivy.market.constants.CommonConstants.REQUESTED_BY;

@Log4j2
@Component
public class MarketHeaderInterceptor implements HandlerInterceptor {

    @Value("${request.header}")
    private String requestHeader;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.warn(request.getHeader(REQUESTED_BY));
        log.warn(requestHeader);

        if (!requestHeader.equals(request.getHeader(REQUESTED_BY))) {
            request.setAttribute(REQUESTED_BY, "ivy");
        }
        return true;
    }
}
