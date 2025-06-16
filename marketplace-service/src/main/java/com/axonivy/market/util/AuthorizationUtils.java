package com.axonivy.market.util;

import com.axonivy.market.constants.CommonConstants;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;

@Log4j2
@Component
public class AuthorizationUtils implements ConstraintValidator<ValidUrl, String> {
  @Value("${allowed.urls}")
  private List<String> allowedUrls;

  public static String getBearerToken(String authorizationHeader) {
    String token = null;
    if (StringUtils.defaultIfEmpty(authorizationHeader, StringUtils.EMPTY).startsWith(CommonConstants.BEARER)) {
      token = authorizationHeader.substring(CommonConstants.BEARER.length()).trim(); // Remove "Bearer " prefix
    }
    return token;
  }

  @Override
  public boolean isValid(String url, ConstraintValidatorContext context) {
    try {
      URI uri = new URI(url);
      String host = uri.getHost();
      if (host == null) return false;
      InetAddress address = InetAddress.getByName(host);
      if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isSiteLocalAddress()) {
        return false;
      }

      return allowedUrls.stream().anyMatch(url::startsWith);

    } catch (URISyntaxException | UnknownHostException e) {
      return false;
    }
  }
}
