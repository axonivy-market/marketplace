package com.axonivy.market.service;

import com.axonivy.market.model.PasskeyAssertionOptionsRequest;
import com.axonivy.market.model.PasskeyCredentialRequest;
import com.axonivy.market.model.UserInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

public interface AdminPasskeyService {
  Map<String, Object> beginRegistration(UserInfo currentUser, HttpServletRequest request, HttpServletResponse response);

  UserInfo finishRegistration(UserInfo currentUser, PasskeyCredentialRequest credentialRequest, HttpServletRequest request);

  Map<String, Object> beginAuthentication(PasskeyAssertionOptionsRequest optionsRequest, HttpServletRequest request,
      HttpServletResponse response);

  UserInfo finishAuthentication(PasskeyCredentialRequest credentialRequest, HttpServletRequest request,
      HttpServletResponse response);
}
