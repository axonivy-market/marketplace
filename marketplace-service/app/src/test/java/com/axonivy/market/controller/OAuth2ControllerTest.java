package com.axonivy.market.controller;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.aop.aspect.AuthorizedAspect;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.model.Oauth2AuthorizationCode;
import com.axonivy.market.model.UserInfo;
import com.axonivy.market.service.OAuth2Service;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerWebMvcTest(OAuth2Controller.class)
class OAuth2ControllerTest extends WebMvcControllerTestSupport {

  private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

  @MockitoBean
  private OAuth2Service oAuth2Service;

  @Test
  void testGitHubLoginSuccess() throws Exception {
    Oauth2AuthorizationCode oauth2AuthorizationCode = new Oauth2AuthorizationCode();
    oauth2AuthorizationCode.setCode("sampleCode");
    when(oAuth2Service.loginToGitHubAndGetJWT(any())).thenReturn(JWT_TOKEN);

    mockMvc.perform(post("/auth/github/login")
            .with(requestedByHeader())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(oauth2AuthorizationCode)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value(JWT_TOKEN));
  }

  @Test
  void testGitHubLoginOauth2ExchangeCodeException() throws Exception {
    Oauth2AuthorizationCode oauth2AuthorizationCode = new Oauth2AuthorizationCode();
    oauth2AuthorizationCode.setCode("sampleCode");

    mockMvc.perform(post("/auth/github/login")
            .with(requestedByHeader())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(oauth2AuthorizationCode)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void testRequestAccessSuccess() throws Exception {
    var mockUserInfo = getMockUserInfo();
    when(oAuth2Service.validateTokenAndGenerateJWT(JWT_TOKEN)).thenReturn(mockUserInfo);

    mockMvc.perform(post("/auth/github/request-access")
            .with(requestedByHeader())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Map.of(GitHubConstants.Json.TOKEN, JWT_TOKEN))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value(JWT_TOKEN));
  }

  @Test
  void testValidateAuthorizationCode() throws Exception {
    mockMvc.perform(put("/auth/github/validate-token")
            .with(requestedByHeader())
            .requestAttr(AuthorizedAspect.VALIDATED_TOKEN_ATTRIBUTE, JWT_TOKEN))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(true));
  }
}
