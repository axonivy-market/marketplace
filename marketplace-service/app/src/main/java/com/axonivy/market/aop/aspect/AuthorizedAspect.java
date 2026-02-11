package com.axonivy.market.aop.aspect;

import com.axonivy.market.aop.annotation.Authorized;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.constants.RequestParamConstants;
import com.axonivy.market.exceptions.model.Oauth2ExchangeCodeException;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import static org.springframework.http.HttpHeaders.*;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Log4j2
@Aspect
@Component
@RequiredArgsConstructor
public class AuthorizedAspect {

  public static final String VALIDATED_TOKEN_ATTRIBUTE = "validatedAccessToken";

  private final JwtService jwtService;
  private final GitHubService gitHubService;

  @Around("@annotation(authorized)")
  public Object validateAuthorization(ProceedingJoinPoint joinPoint, Authorized authorized) throws Throwable {
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attributes == null || authorized == null) {
      throw throwInvalidAuthorizationException();
    }

    HttpServletRequest request = attributes.getRequest();
    String authorizationHeader = request.getHeader(RequestParamConstants.X_AUTHORIZATION);
    if (authorizationHeader == null) {
      authorizationHeader = request.getHeader(AUTHORIZATION);
    }
    if (StringUtils.isBlank(authorizationHeader) || !authorizationHeader.startsWith(CommonConstants.BEARER)) {
      throw new Oauth2ExchangeCodeException(HttpStatus.UNAUTHORIZED.name(),
          "Missing Authorization header or invalid Bearer token");
    }
    // First, get RAW token from JWT, then validate user in org or team by defined scope.
    // Also can throw ExpiredJwtException, MalformedJwtException, SignatureException, etc.
    // Before proceeding to the controller method
    String token = jwtService.getRawAccessToken(authorizationHeader);
    if (ObjectUtils.isEmpty(token)) {
      throw throwInvalidAuthorizationException();
    }

    if (Authorized.AuthorizationScope.ORGANIZATION_TEAM == authorized.scope()) {
      gitHubService.validateUserInOrganizationAndTeam(token,
        GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME,
        GitHubConstants.AXONIVY_MARKET_TEAM_NAME);
    }

    request.setAttribute(VALIDATED_TOKEN_ATTRIBUTE, token);
    return joinPoint.proceed();
  }

  private Oauth2ExchangeCodeException throwInvalidAuthorizationException() {
    throw new Oauth2ExchangeCodeException(HttpStatus.BAD_REQUEST.name(),
        "Invalid Authorization header");
  }
}
