package com.axonivy.market.github.util;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.github.model.CodeScanning;
import com.axonivy.market.github.model.Dependabot;
import com.axonivy.market.github.model.SecretScanning;
import com.axonivy.market.util.MavenUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.PagedIterable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.axonivy.market.constants.MetaConstants.META_FILE;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GitHubUtils {

  public static long getGHCommitDate(GHCommit commit) {
    long commitTime = 0L;
    if (commit != null) {
      try {
        commitTime = commit.getCommitDate().getTime();
      } catch (Exception e) {
        log.error("Check last commit failed", e);
      }
    }
    return commitTime;
  }

  public static String getDownloadUrl(GHContent content) {
    try {
      return content.getDownloadUrl();
    } catch (IOException e) {
      log.error("Cannot get DownloadURl from GHContent: ", e);
    }
    return StringUtils.EMPTY;
  }

  public static <T> List<T> mapPagedIteratorToList(PagedIterable<T> paged) {
    if (paged != null) {
      try {
        return paged.toList();
      } catch (IOException e) {
        log.error("Cannot parse to list for pagediterable: ", e);
      }
    }
    return List.of();
  }

  public static String convertArtifactIdToName(String artifactId) {
    if (StringUtils.isBlank(artifactId)) {
      return StringUtils.EMPTY;
    }
    return Arrays.stream(artifactId.split(CommonConstants.DASH_SEPARATOR))
        .map(part -> part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase())
        .collect(Collectors.joining(CommonConstants.SPACE_SEPARATOR));
  }

  public static int sortMetaJsonFirst(String fileName1, String fileName2) {
    if (fileName1.endsWith(META_FILE))
      return -1;
    if (fileName2.endsWith(META_FILE))
      return 1;
    return fileName1.compareTo(fileName2);
  }

  public static void findImages(List<GHContent> files, List<GHContent> images) {
    for (GHContent file : files) {
      if (file.isDirectory()) {
        findImagesInDirectory(file, images);
      } else if (file.getName().toLowerCase().matches(CommonConstants.IMAGE_EXTENSION)) {
        images.add(file);
      }
    }
  }

  private static void findImagesInDirectory(GHContent file, List<GHContent> images) {
    try {
      List<GHContent> childrenFiles = file.listDirectoryContent().toList();
      findImages(childrenFiles, images);
    } catch (IOException e) {
      log.error(e.getMessage());
    }
  }

  public static List<Artifact> convertProductJsonToMavenProductInfo(GHContent content) throws IOException {
    InputStream contentStream = extractedContentStream(content);
    if (Objects.isNull(contentStream)) {
      return new ArrayList<>();
    }
    return MavenUtils.extractMavenArtifactsFromContentStream(contentStream);
  }

  public static InputStream extractedContentStream(GHContent content) {
    try {
      return content.read();
    } catch (IOException | NullPointerException e) {
      log.warn("Can not read the current content: {}", e.getMessage());
      return null;
    }
  }

  public static Dependabot getDependabotAlerts(GHRepository repo, GHOrganization organization, String accessToken) {
    return fetchAlerts(
        accessToken,
        String.format(GitHubConstants.Url.REPO_DEPENDABOT_ALERTS_OPEN, organization.getLogin(), repo.getName()),
        alerts -> {
          Dependabot dependabot = new Dependabot();
          Map<String, Integer> severityMap = new HashMap<>();
          for (Map<String, Object> alert : alerts) {
            Object advisoryObj = alert.get(GitHubConstants.Json.SEVERITY_ADVISORY);
            if (advisoryObj instanceof Map<?, ?> securityAdvisory) {
              String severity = (String) securityAdvisory.get(GitHubConstants.Json.SEVERITY);
              if (severity != null) {
                severityMap.put(severity, severityMap.getOrDefault(severity, 0) + 1);
              }
            }
          }
          dependabot.setAlerts(severityMap);
          return dependabot;
        },
        Dependabot::new
    );
  }

  public static SecretScanning getNumberOfSecretScanningAlerts(GHRepository repo, GHOrganization organization, String accessToken) {
    return fetchAlerts(
        accessToken,
        String.format(GitHubConstants.Url.REPO_SECRET_SCANNING_ALERTS_OPEN, organization.getLogin(), repo.getName()),
        alerts -> {
          SecretScanning secretScanning = new SecretScanning();
          secretScanning.setNumberOfAlerts(alerts.size());
          return secretScanning;
        },
        SecretScanning::new
    );
  }

  public static CodeScanning getCodeScanningAlerts(GHRepository repo, GHOrganization organization, String accessToken) {
    return fetchAlerts(
        accessToken,
        String.format(GitHubConstants.Url.REPO_CODE_SCANNING_ALERTS_OPEN, organization.getLogin(), repo.getName()),
        alerts -> {
          CodeScanning codeScanning = new CodeScanning();
          Map<String, Integer> codeScanningMap = new HashMap<>();
          for (Map<String, Object> alert : alerts) {
            Object ruleObj = alert.get(GitHubConstants.Json.RULE);
            if (ruleObj instanceof Map<?, ?> rule) {
              String severity = (String) rule.get(GitHubConstants.Json.SECURITY_SEVERITY_LEVEL);
              if (severity != null) {
                codeScanningMap.put(severity, codeScanningMap.getOrDefault(severity, 0) + 1);
              }
            }
          }
          codeScanning.setAlerts(codeScanningMap);
          return codeScanning;
        },
        CodeScanning::new
    );
  }

  private static <T> T fetchAlerts(
      String accessToken,
      String url,
      Function<List<Map<String, Object>>, T> mapAlerts,
      Supplier<T> defaultInstanceSupplier
  ) {
    T instance = defaultInstanceSupplier.get();
    try {
      ResponseEntity<List<Map<String, Object>>> response = fetchApiResponseAsList(accessToken, url);
      instance = mapAlerts.apply(response.getBody() != null ? response.getBody() : List.of());
      setStatus(instance, com.axonivy.market.enums.AccessLevel.ENABLED);
    } catch (HttpClientErrorException.Forbidden e) {
      setStatus(instance, com.axonivy.market.enums.AccessLevel.DISABLED);
    } catch (HttpClientErrorException.NotFound e) {
      setStatus(instance, com.axonivy.market.enums.AccessLevel.NO_PERMISSION);
    }
    return instance;
  }

  private static void setStatus(Object instance, com.axonivy.market.enums.AccessLevel status) {
    if (instance instanceof Dependabot dependabot) {
      dependabot.setStatus(status);
    } else if (instance instanceof SecretScanning secretScanning) {
      secretScanning.setStatus(status);
    } else if (instance instanceof CodeScanning codeScanning) {
      codeScanning.setStatus(status);
    }
  }

  public static ResponseEntity<List<Map<String, Object>>> fetchApiResponseAsList(String accessToken,
      String url) throws RestClientException {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    HttpEntity<String> entity = new HttpEntity<>(headers);

    return new RestTemplate().exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
    });
  }
}
