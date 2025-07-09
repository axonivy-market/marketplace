package com.axonivy.market.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpFetchingUtils {
  private static final RestTemplate restTemplate = new RestTemplate();
  private static final String UNKNOWN_FILE_NAME = "unknown_file";

  public static ResponseEntity<Resource> fetchResourceUrl(String url) {
    try {
      return restTemplate.exchange(url, HttpMethod.GET, null, Resource.class);
    } catch (Exception e) {
       log.warn("Failed to fetch resource from URL: {}", url, e);
      return null;
    }
  }

  public static byte[] getFileAsBytes(String url) {
    try {
      return restTemplate.getForObject(url, byte[].class);
    } catch (Exception e) {
       log.warn("Failed to fetch bytes from URL: {}", url, e);
      return new byte[0];
    }
  }

  public static String getFileAsString(String url) {
    try {
      return restTemplate.getForObject(url, String.class);
    } catch (Exception e) {
       log.warn("Failed to fetch string from URL: {}", url, e);
      return StringUtils.EMPTY;
    }
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
