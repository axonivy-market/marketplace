package com.axonivy.market.util;

import com.axonivy.market.constants.CommonConstants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthorizationUtils {
  private static final List<String> ALLOWED_DOMAINS = List.of("market.axonivy.com", "maven.axonivy.com");

  public static String getBearerToken(String authorizationHeader) {
    String token = null;
    if (StringUtils.defaultIfEmpty(authorizationHeader, StringUtils.EMPTY).startsWith(CommonConstants.BEARER)) {
      token = authorizationHeader.substring(CommonConstants.BEARER.length()).trim(); // Remove "Bearer " prefix
    }
    return token;
  }

  public static boolean isAllowedUrl(String url) {
    try {
      URI uri = new URI(url);
      String host = uri.getHost();
      return ALLOWED_DOMAINS.contains(host);
    } catch (URISyntaxException e) {
      return false;
    }
  }
}
