package com.axonivy.market.util.validator;

import com.axonivy.market.constants.CommonConstants;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import lombok.NoArgsConstructor;
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
@NoArgsConstructor
public class AuthorizationUtils implements ConstraintValidator<ValidUrl, String> {
  @Value("${allowed.urls}")
  private List<String> allowedUrls;

  public static String getBearerToken(String authorizationHeader) {
    String token = null;
    if (StringUtils.defaultIfEmpty(authorizationHeader, StringUtils.EMPTY).startsWith(CommonConstants.BEARER)) {
      // Remove "Bearer " prefix
      token = authorizationHeader.substring(CommonConstants.BEARER.length()).trim();
    }
    return token;
  }

  @Override
  public boolean isValid(String url, ConstraintValidatorContext context) {
    if (url == null || url.isBlank()) {
      return false;
    }

    try {
      var uri = new URI(url);
      var host = uri.getHost();
      if (host == null) {
        return false;
      }

      var address = InetAddress.getByName(host);

      if (isPrivateAddress(address)) {
        return false;
      }

      return allowedUrls.stream().anyMatch(url::startsWith);

    } catch (URISyntaxException | UnknownHostException e) {
      log.warn("URL validation failed: {}", url, e);
      return false;
    }
  }

  private boolean isPrivateAddress(InetAddress address) {
    return address.isAnyLocalAddress()
        || address.isLoopbackAddress()
        || address.isSiteLocalAddress();
  }

  public  String resolveTrustedUrl(String inputUrl) {
    return allowedUrls.stream()
        .filter(inputUrl::startsWith)
        .findFirst()
        .map(allowedBase -> inputUrl)
        .orElseThrow(() -> new IllegalArgumentException("URL not from allowed sources"));
  }
}
