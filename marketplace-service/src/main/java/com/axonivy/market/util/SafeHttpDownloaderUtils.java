package com.axonivy.market.util;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class SafeHttpDownloaderUtils {

  @Value("${allowed.urls}")
  private String[] allowedUrlsArray;

  private Set<String> allowedBaseUrls;

  private static final int TIMEOUT_MILLISECONDS = 5000;

  private RestTemplate restTemplate;

  @PostConstruct
  public void init() {
    allowedBaseUrls = new HashSet<>(Arrays.asList(allowedUrlsArray));
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(TIMEOUT_MILLISECONDS);
    factory.setReadTimeout(TIMEOUT_MILLISECONDS);
    restTemplate = new RestTemplate(factory);
  }

  public void validateUri(URI uri) {
    String host = uri.getHost();
    if (host == null) {
      throw new IllegalArgumentException("Invalid URI: missing host");
    }

    try {
      InetAddress address = InetAddress.getByName(host);
      if (address.isAnyLocalAddress() ||
          address.isLoopbackAddress() ||
          address.isSiteLocalAddress()) {
        throw new IllegalArgumentException("Internal IPs are not allowed");
      }
    } catch (UnknownHostException e) {
      throw new IllegalArgumentException("Unknown host: " + host, e);
    }

    String uriStr = uri.toString();
    boolean isAllowed = allowedBaseUrls.stream().anyMatch(uriStr::startsWith);
    if (!isAllowed) {
      throw new IllegalArgumentException("URL not in allowed list: " + uriStr);
    }
  }
}