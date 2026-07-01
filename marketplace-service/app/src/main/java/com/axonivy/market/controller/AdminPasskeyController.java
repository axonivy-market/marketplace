package com.axonivy.market.controller;

import com.axonivy.market.model.PasskeyAssertionOptionsRequest;
import com.axonivy.market.model.PasskeyCredentialRequest;
import com.axonivy.market.model.UserInfo;
import com.axonivy.market.service.AdminPasskeyService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.webauthn.management.WebAuthnRelyingPartyOperations;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.axonivy.market.constants.RequestMappingConstants.ADMIN_AUTH_V2;
import static com.axonivy.market.constants.RequestMappingConstants.AUTHENTICATE;
import static com.axonivy.market.constants.RequestMappingConstants.COMPLETE;
import static com.axonivy.market.constants.RequestMappingConstants.OPTIONS;
import static com.axonivy.market.constants.RequestMappingConstants.PASSKEY;
import static com.axonivy.market.constants.RequestMappingConstants.REGISTER;

@Hidden
@RestController
@RequiredArgsConstructor
@ConditionalOnBean(WebAuthnRelyingPartyOperations.class)
@RequestMapping(ADMIN_AUTH_V2 + PASSKEY)
public class AdminPasskeyController {
  private final AdminPasskeyService adminPasskeyService;

  @PostMapping(REGISTER + OPTIONS)
  public Map<String, Object> beginRegistration(@AuthenticationPrincipal UserInfo currentUser, HttpServletRequest request,
      HttpServletResponse response) {
    return adminPasskeyService.beginRegistration(currentUser, request, response);
  }

  @PostMapping(REGISTER + COMPLETE)
  public UserInfo finishRegistration(@AuthenticationPrincipal UserInfo currentUser,
      @RequestBody PasskeyCredentialRequest credentialRequest, HttpServletRequest request) {
    return adminPasskeyService.finishRegistration(currentUser, credentialRequest, request);
  }

  @PostMapping(AUTHENTICATE + OPTIONS)
  public Map<String, Object> beginAuthentication(@RequestBody(required = false) PasskeyAssertionOptionsRequest optionsRequest,
      HttpServletRequest request, HttpServletResponse response) {
    return adminPasskeyService.beginAuthentication(optionsRequest, request, response);
  }

  @PostMapping(AUTHENTICATE + COMPLETE)
  public UserInfo finishAuthentication(@RequestBody PasskeyCredentialRequest credentialRequest,
      HttpServletRequest request, HttpServletResponse response) {
    return adminPasskeyService.finishAuthentication(credentialRequest, request, response);
  }
}
