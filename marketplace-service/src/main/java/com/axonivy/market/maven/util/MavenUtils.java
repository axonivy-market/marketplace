package com.axonivy.market.maven.util;

import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.maven.model.MavenArtifact;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class MavenUtils {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static List<MavenArtifact> getMavenArtifactsFromProductJson(ProductJsonContent productJson) {
    if (Objects.isNull(productJson) || StringUtils.isBlank(productJson.getContent())) {
      return new ArrayList<>();
    }
    InputStream contentStream = IOUtils.toInputStream(productJson.getContent(), StandardCharsets.UTF_8);
    try {
      return extractMavenArtifactsFromContentStream(contentStream);
    } catch (IOException e) {
      log.error("Can not get maven artifacts from Product.json of {}", productJson);
      return new ArrayList<>();
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
}
