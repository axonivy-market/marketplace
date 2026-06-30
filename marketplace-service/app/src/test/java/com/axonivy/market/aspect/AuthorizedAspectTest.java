package com.axonivy.market.aspect;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.*;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.model.UserInfo;
import com.axonivy.market.testutil.MockServletRequestUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.axonivy.market.aop.annotation.Authorized;
import com.axonivy.market.aop.aspect.AuthorizedAspect;
import com.axonivy.market.constants.RequestParamConstants;
import com.axonivy.market.exceptions.model.Oauth2ExchangeCodeException;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.service.JwtService;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class AuthorizedAspectTest {
  @Mock
  private HttpServletRequest request;

  @Mock
  private JwtService jwtService;

  @Mock
  private GitHubService gitHubService;

  @Mock
  private ProceedingJoinPoint joinPoint;

  @Mock
  private Authorized authorized;

  @InjectMocks
  private AuthorizedAspect authorizedAspect;

  @BeforeEach
  void setup() {
    MockServletRequestUtils.bindRequest(request);
  }

  @Test
  void testUnauthorizedThrowsException() {
    when(request.getHeader(AUTHORIZATION)).thenReturn(null);
    when(request.getHeader(RequestParamConstants.X_AUTHORIZATION)).thenReturn(null);

    assertThrows(Oauth2ExchangeCodeException.class,
        () -> authorizedAspect.validateAuthorization(joinPoint, authorized),
        "Should throw Oauth2ExchangeCodeException when no authorization header is present");
  }

  @Test
  void testAuthorizedSuccess() throws Throwable {
    UserInfo mockUser = new UserInfo();
    mockUser.setUsername("test-user");
    mockUser.setGitHubId("123456");

    when(authorized.scope()).thenReturn(Authorized.AuthorizationScope.ORGANIZATION_TEAM);
    when(request.getHeader(RequestParamConstants.X_AUTHORIZATION)).thenReturn("Bearer valid-token");
    when(jwtService.getRawAccessToken("Bearer valid-token")).thenReturn("valid-token");
    when(gitHubService.validateUserInOrganizationAndTeam(
        "valid-token",
        GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME,
        GitHubConstants.AXONIVY_MARKET_TEAM_NAME
    )).thenReturn(mockUser);

    when(joinPoint.proceed()).thenReturn("success");

    Object result = authorizedAspect.validateAuthorization(joinPoint, authorized);

    assertEquals("success", result, "Should return the result from proceed when authorized");

    verify(request).setAttribute(AuthorizedAspect.USERNAME_ATTRIBUTE, "test-user");
    verify(request).setAttribute(AuthorizedAspect.GITHUB_USER_ID_ATTRIBUTE, "123456");
    verify(request).setAttribute(AuthorizedAspect.VALIDATED_TOKEN_ATTRIBUTE, "valid-token");
    verify(joinPoint).proceed();
  }

  @Test
  void testValidateAuthorizationWhenRequestAttributesNullShouldThrowException() {
    MockServletRequestUtils.resetRequestAttributes();

    Oauth2ExchangeCodeException exception = assertThrows(
        Oauth2ExchangeCodeException.class,
        () -> authorizedAspect.validateAuthorization(joinPoint, authorized),
        "Should throw Oauth2ExchangeCodeException when RequestContextHolder has no attributes"
    );

    assertEquals(HttpStatus.BAD_REQUEST.name(), exception.getError(),
        "Error code should be BAD_REQUEST when request attributes are missing");

    MockServletRequestUtils.bindRequest(request);
  }

  @Test
  void testValidateAuthorizationWhenAuthorizedAnnotationNullShouldThrowException() {
    Oauth2ExchangeCodeException exception = assertThrows(
        Oauth2ExchangeCodeException.class,
        () -> authorizedAspect.validateAuthorization(joinPoint, null),
        "Should throw Oauth2ExchangeCodeException when Authorized annotation is null"
    );

    assertEquals(HttpStatus.BAD_REQUEST.name(), exception.getError(),
        "Error code should be BAD_REQUEST when Authorized annotation is null");
  }

  @Test
  void testValidateAuthorizationWhenTokenIsNullShouldThrowException() throws Throwable {
    when(request.getHeader(RequestParamConstants.X_AUTHORIZATION)).thenReturn("Bearer invalid-token");
    when(jwtService.getRawAccessToken("Bearer invalid-token")).thenReturn(null);

    Oauth2ExchangeCodeException exception = assertThrows(
        Oauth2ExchangeCodeException.class,
        () -> authorizedAspect.validateAuthorization(joinPoint, authorized),
        "Should throw exception when extracted token is null"
    );

    assertEquals(HttpStatus.BAD_REQUEST.name(), exception.getError(),
        "Error code should be BAD_REQUEST when token is null");

    verify(jwtService).getRawAccessToken("Bearer invalid-token");
    verifyNoInteractions(gitHubService);
    verify(joinPoint, never()).proceed();
  }

  @Test
  void testValidateAuthorizationWhenTokenIsEmptyShouldThrowException() throws Throwable {
    when(request.getHeader(RequestParamConstants.X_AUTHORIZATION)).thenReturn("Bearer invalid-token");
    when(jwtService.getRawAccessToken("Bearer invalid-token")).thenReturn("");

    Oauth2ExchangeCodeException exception = assertThrows(
        Oauth2ExchangeCodeException.class,
        () -> authorizedAspect.validateAuthorization(joinPoint, authorized),
        "Should throw exception when extracted token is empty"
    );

    assertEquals(HttpStatus.BAD_REQUEST.name(), exception.getError(),
        "Error code should be BAD_REQUEST when Token is empty");

    verify(jwtService).getRawAccessToken("Bearer invalid-token");
    verifyNoInteractions(gitHubService);
    verify(joinPoint, never()).proceed();
  }

  @Test
  void testValidateAuthorizationWhenUsernameIsBlankShouldThrowException() throws Throwable {
    UserInfo mockUser = new UserInfo();
    mockUser.setUsername("");
    mockUser.setGitHubId("123456");

    when(authorized.scope()).thenReturn(Authorized.AuthorizationScope.ORGANIZATION_TEAM);
    when(request.getHeader(RequestParamConstants.X_AUTHORIZATION))
        .thenReturn("Bearer valid-token");
    when(jwtService.getRawAccessToken("Bearer valid-token"))
        .thenReturn("valid-token");

    when(gitHubService.validateUserInOrganizationAndTeam(
        "valid-token",
        GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME,
        GitHubConstants.AXONIVY_MARKET_TEAM_NAME
    )).thenReturn(mockUser);

    Oauth2ExchangeCodeException exception = assertThrows(
        Oauth2ExchangeCodeException.class,
        () -> authorizedAspect.validateAuthorization(joinPoint, authorized),
        "Should throw exception when username is blank"
    );

    assertEquals(HttpStatus.UNAUTHORIZED.name(), exception.getError(),
        "Error code should be UNAUTHORIZED when username is blank");

    verify(joinPoint, never()).proceed();
  }

  @Test
  void testValidateAuthorizationWhenGithubUserIdIsBlankShouldThrowException() throws Throwable {
    UserInfo mockUser = new UserInfo();
    mockUser.setUsername("test-user");
    mockUser.setGitHubId("");

    when(authorized.scope()).thenReturn(Authorized.AuthorizationScope.ORGANIZATION_TEAM);
    when(request.getHeader(RequestParamConstants.X_AUTHORIZATION))
        .thenReturn("Bearer valid-token");
    when(jwtService.getRawAccessToken("Bearer valid-token"))
        .thenReturn("valid-token");

    when(gitHubService.validateUserInOrganizationAndTeam(
        "valid-token",
        GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME,
        GitHubConstants.AXONIVY_MARKET_TEAM_NAME
    )).thenReturn(mockUser);

    Oauth2ExchangeCodeException exception = assertThrows(
        Oauth2ExchangeCodeException.class,
        () -> authorizedAspect.validateAuthorization(joinPoint, authorized),
        "Should throw exception when GitHub user ID is blank"
    );

    assertEquals(HttpStatus.UNAUTHORIZED.name(), exception.getError(),
        "Error code should be UNAUTHORIZED when github user id is blank");

    verify(joinPoint, never()).proceed();
  }
}
