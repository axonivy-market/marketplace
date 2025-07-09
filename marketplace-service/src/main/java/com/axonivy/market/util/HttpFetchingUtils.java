package com.axonivy.market.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
@Slf4j
public class HttpFetchingUtils {
  private static RestTemplate restTemplate;
  private static final String UNKNOWN_FILE_NAME = "unknown_file";

  private HttpFetchingUtils() {
    restTemplate = new RestTemplate();
  }

  public static ResponseEntity<Resource> fetchResourceUrl(String url) {
    return restTemplate.exchange(url, HttpMethod.GET, null, Resource.class);
  }

  public static byte[] downloadFile(String url) {
    return restTemplate.getForObject(url, byte[].class);
  }

  public static String extractFileNameFromUrl(String fileUrl) {
    try {
      String path = new URI(fileUrl).getPath();
      return Paths.get(path).getFileName().toString();
    } catch (URISyntaxException e) {
      log.warn("Can not get the file the from path {}", fileUrl);
      return UNKNOWN_FILE_NAME;
    }
  }
}
