package com.axonivy.market.github.util;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.enums.NonStandardProduct;
import com.axonivy.market.github.model.MavenArtifact;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.PagedIterable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.axonivy.market.constants.MetaConstants.META_FILE;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GitHubUtils {
  private static final ObjectMapper objectMapper = new ObjectMapper();

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

  public static String getNonStandardProductFilePath(String productId) {
    return NonStandardProduct.findById(productId).getPathToProductFolder();
  }

  public static String getNonStandardImageFolder(String productId) {
    return NonStandardProduct.findById(productId).getPathToImageFolder();
  }

  public static String extractMessageFromExceptionMessage(String exceptionMessage) {
    String json = extractJson(exceptionMessage);
    String key = "\"message\":\"";
    int startIndex = json.indexOf(key);
    if (startIndex != -1) {
      startIndex += key.length();
      int endIndex = json.indexOf("\"", startIndex);
      if (endIndex != -1) {
        return json.substring(startIndex, endIndex);
      }
    }
    return StringUtils.EMPTY;
  }

  public static String extractJson(String text) {
    int start = text.indexOf("{");
    int end = text.lastIndexOf("}") + 1;
    if (start != -1 && end != -1) {
      return text.substring(start, end);
    }
    return StringUtils.EMPTY;
  }

  public static int sortMetaJsonFirst(String fileName1, String fileName2) {
    if (fileName1.endsWith(META_FILE))
      return -1;
    if (fileName2.endsWith(META_FILE))
      return 1;
    return fileName1.compareTo(fileName2);
  }

  public static List<MavenArtifact> convertProductJsonToMavenProductInfo(GHContent content) throws IOException {
    InputStream contentStream = extractedContentStream(content);
    if (Objects.isNull(contentStream)) {
      return new ArrayList<>();
    }
    return extractMavenArtifactsFromContentStream(contentStream);
  }

  public static List<MavenArtifact> extractMavenArtifactsFromContentStream(InputStream contentStream) throws IOException {
    List<MavenArtifact> artifacts = new ArrayList<>();
    JsonNode rootNode = objectMapper.readTree(contentStream);
    JsonNode installersNode = rootNode.path(ProductJsonConstants.INSTALLERS);

    for (JsonNode mavenNode : installersNode) {
      JsonNode dataNode = mavenNode.path(ProductJsonConstants.DATA);

      // Not convert to artifact if id of node is not maven-import or maven-dependency
      List<String> installerIdsToDisplay = List.of(ProductJsonConstants.MAVEN_DEPENDENCY_INSTALLER_ID,
          ProductJsonConstants.MAVEN_IMPORT_INSTALLER_ID);
      if (!installerIdsToDisplay.contains(mavenNode.path(ProductJsonConstants.ID).asText())) {
        continue;
      }

      // Extract repository URL
      JsonNode repositoriesNode = dataNode.path(ProductJsonConstants.REPOSITORIES);
      String repoUrl = repositoriesNode.get(0).path(ProductJsonConstants.URL).asText();

      // Process projects
      if (dataNode.has(ProductJsonConstants.PROJECTS)) {
        extractMavenArtifactFromJsonNode(dataNode, false, artifacts, repoUrl);
      }

      // Process dependencies
      if (dataNode.has(ProductJsonConstants.DEPENDENCIES)) {
        extractMavenArtifactFromJsonNode(dataNode, true, artifacts, repoUrl);
      }
    }
    return artifacts;
  }

  public static InputStream extractedContentStream(GHContent content) {
    try {
      return content.read();
    } catch (IOException | NullPointerException e) {
      log.warn("Can not read the current content: {}", e.getMessage());
      return null;
    }
  }

  public static void extractMavenArtifactFromJsonNode(JsonNode dataNode, boolean isDependency,
      List<MavenArtifact> artifacts
      , String repoUrl) {
    String nodeName = ProductJsonConstants.PROJECTS;
    if (isDependency) {
      nodeName = ProductJsonConstants.DEPENDENCIES;
    }
    JsonNode dependenciesNode = dataNode.path(nodeName);
    for (JsonNode dependencyNode : dependenciesNode) {
      MavenArtifact artifact = createArtifactFromJsonNode(dependencyNode, repoUrl, isDependency);
      artifacts.add(artifact);
    }
  }

  public static MavenArtifact createArtifactFromJsonNode(JsonNode node, String repoUrl, boolean isDependency) {
    MavenArtifact artifact = new MavenArtifact();
    artifact.setRepoUrl(repoUrl);
    artifact.setIsDependency(isDependency);
    artifact.setGroupId(node.path(ProductJsonConstants.GROUP_ID).asText());
    artifact.setArtifactId(node.path(ProductJsonConstants.ARTIFACT_ID).asText());
    artifact.setType(node.path(ProductJsonConstants.TYPE).asText());
    artifact.setIsProductArtifact(true);
    return artifact;
  }
}
