package com.axonivy.market.util;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class HttpFetchingUtils {
  private static final RestTemplate restTemplate = new RestTemplate();

  private HttpFetchingUtils() {
  }

  public static ResponseEntity<Resource> fetchResourceUrl(String url) {
    return restTemplate.exchange(url, HttpMethod.GET, null, Resource.class);
  }

  public static byte[] downloadFile(String url) {
    return restTemplate.getForObject(url, byte[].class);
  }
}
