package com.axonivy.market.service;

import com.axonivy.market.model.PasskeyAssertionOptionsRequest;
import com.axonivy.market.model.PasskeyCredentialRequest;
import com.axonivy.market.model.UserInfo;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AdminPasskeyService {
  JsonNode beginRegistration(UserInfo currentUser, HttpServletRequest request, HttpServletResponse response);

  UserInfo finishRegistration(UserInfo currentUser, PasskeyCredentialRequest credentialRequest, HttpServletRequest request);

  JsonNode beginAuthentication(PasskeyAssertionOptionsRequest optionsRequest, HttpServletRequest request,
      HttpServletResponse response);

  UserInfo finishAuthentication(PasskeyCredentialRequest credentialRequest, HttpServletRequest request,
      HttpServletResponse response);
}
