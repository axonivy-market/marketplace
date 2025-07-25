package com.axonivy.market.util.validator;

import com.axonivy.market.constants.CommonConstants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthorizationUtils {
  public static String getBearerToken(String authorizationHeader) {
    String token = null;
    if (StringUtils.defaultIfEmpty(authorizationHeader, StringUtils.EMPTY).startsWith(CommonConstants.BEARER)) {
      // Remove "Bearer " prefix
      token = authorizationHeader.substring(CommonConstants.BEARER.length()).trim();
    }
    return token;
  }
}
