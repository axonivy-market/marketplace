package com.axonivy.market.github.service.impl;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHTag;
import org.springframework.stereotype.Service;

import com.axonivy.market.github.service.GitHubService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Log4j2
@Service
public class GHAxonIvyProductRepoServiceImpl implements GHAxonIvyProductRepoService {
  private GHOrganization organization;
  private final GitHubService gitHubService;
  private String repoUrl;
  private static final ObjectMapper objectMapper = new ObjectMapper();


  public GHAxonIvyProductRepoServiceImpl(GitHubService gitHubService) {
    this.gitHubService = gitHubService;
  }

  @Override
  public List<MavenArtifact> convertProductJsonToMavenProductInfo(GHContent content) throws IOException {
    List<MavenArtifact> artifacts = new ArrayList<>();
    InputStream contentStream = extractedContentStream(content);
    if (Objects.isNull(contentStream)) {
      return artifacts;
    }

    JsonNode rootNode = objectMapper.readTree(contentStream);
    JsonNode installersNode = rootNode.path(ProductJsonConstants.INSTALLERS);

    for (JsonNode mavenNode : installersNode) {
      JsonNode dataNode = mavenNode.path(ProductJsonConstants.DATA);

      // Not convert to artifact if id of node is not maven-import or maven-dependency
      List<String> installerIdsToDisplay = List.of(ProductJsonConstants.MAVEN_DEPENDENCY_INSTALLER_ID, ProductJsonConstants.MAVEN_IMPORT_INSTALLER_ID);
      if(!installerIdsToDisplay.contains(mavenNode.path(ProductJsonConstants.ID).asText())){
        continue;
      }

      // Extract repository URL
      JsonNode repositoriesNode = dataNode.path(ProductJsonConstants.REPOSITORIES);
      repoUrl = repositoriesNode.get(0).path(ProductJsonConstants.URL).asText();

      // Process projects
      if (dataNode.has(ProductJsonConstants.PROJECTS)) {
        extractMavenArtifactFromJsonNode(dataNode, false, artifacts);
      }

      // Process dependencies
      if (dataNode.has(ProductJsonConstants.DEPENDENCIES)) {
        extractMavenArtifactFromJsonNode(dataNode, true, artifacts);
      }
    }
    return artifacts;
  }

  public InputStream extractedContentStream(GHContent content) {
    try {
      return content.read();
    } catch (IOException | NullPointerException e) {
      log.warn("Can not read the current content: {}", e.getMessage());
      return null;
    }
  }

  public void extractMavenArtifactFromJsonNode(JsonNode dataNode, boolean isDependency, List<MavenArtifact> artifacts) {
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

  public MavenArtifact createArtifactFromJsonNode(JsonNode node, String repoUrl, boolean isDependency) {
    MavenArtifact artifact = new MavenArtifact();
    artifact.setRepoUrl(repoUrl);
    artifact.setIsDependency(isDependency);
    artifact.setGroupId(node.path(ProductJsonConstants.GROUP_ID).asText());
    artifact.setArtifactId(node.path(ProductJsonConstants.ARTIFACT_ID).asText());
    artifact.setType(node.path(ProductJsonConstants.TYPE).asText());
    artifact.setIsProductArtifact(true);
    return artifact;
  }

  @Override
  public GHContent getContentFromGHRepoAndTag(String repoName, String filePath, String tagVersion) {
    try {
      return getOrganization().getRepository(repoName).getFileContent(filePath, tagVersion);
    } catch (IOException e) {
      log.error("Cannot Get Content From File Directory", e);
      return null;
    }
  }

  public GHOrganization getOrganization() throws IOException {
    if (organization == null) {
      organization = gitHubService.getOrganization(GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME);
    }
    return organization;
  }

  @Override
  public List<GHTag> getAllTagsFromRepoName(String repoName) throws IOException {
    return getOrganization().getRepository(repoName).listTags().toList();
  }
}
