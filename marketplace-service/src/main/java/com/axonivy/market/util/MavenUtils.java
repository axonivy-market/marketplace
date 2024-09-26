package com.axonivy.market.util;

import com.axonivy.market.bo.ArchivedArtifact;
import com.axonivy.market.bo.Artifact;
import com.axonivy.market.comparator.MavenVersionComparator;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.model.MavenArtifactModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Log4j2
public class MavenUtils {
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final RestTemplate restTemplate = new RestTemplate();

  private MavenUtils() {}

  public static List<Artifact> getMavenArtifactsFromProductJson(ProductJsonContent productJson) {
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

  public static Artifact createArtifactFromJsonNode(JsonNode node, String repoUrl, boolean isDependency) {
    Artifact artifact = new Artifact();
    artifact.setRepoUrl(repoUrl);
    artifact.setIsDependency(isDependency);
    artifact.setGroupId(getTextValueFromNodeAndPath(node, ProductJsonConstants.GROUP_ID));
    artifact.setArtifactId(getTextValueFromNodeAndPath(node, ProductJsonConstants.ARTIFACT_ID));
    artifact.setType(getTextValueFromNodeAndPath(node, ProductJsonConstants.TYPE));
    artifact.setIsProductArtifact(true);
    return artifact;
  }

  private static String getTextValueFromNodeAndPath(JsonNode node, String path) {
    return Objects.isNull(node.path(path)) ? StringUtils.EMPTY : node.path(path).asText();
  }

  public static void extractMavenArtifactFromJsonNode(JsonNode dataNode, boolean isDependency, List<Artifact> artifacts,
      String repoUrl) {
    String nodeName = ProductJsonConstants.PROJECTS;
    if (isDependency) {
      nodeName = ProductJsonConstants.DEPENDENCIES;
    }
    JsonNode dependenciesNode = dataNode.path(nodeName);
    for (JsonNode dependencyNode : dependenciesNode) {
      Artifact artifact = createArtifactFromJsonNode(dependencyNode, repoUrl, isDependency);
      artifacts.add(artifact);
    }
  }

  public static List<Artifact> convertProductJsonToMavenProductInfo(Path folderPath) throws IOException {
    Path productJsonPath = folderPath.resolve(ProductJsonConstants.PRODUCT_JSON_FILE);


    if (!(Files.exists(productJsonPath) && Files.isRegularFile(productJsonPath))) {
      log.warn("product.json file not found in the folder: {}", folderPath);
      return new ArrayList<>();
    }

    InputStream contentStream = extractedContentStream(productJsonPath);
    if (Objects.isNull(contentStream)) {
      return new ArrayList<>();
    }
    return extractMavenArtifactsFromContentStream(contentStream);
  }

  public static InputStream extractedContentStream(Path filePath) {
    try {
      return Files.newInputStream(filePath);
    } catch (IOException | NullPointerException e) {
      log.warn("Cannot read the current file: {}", e.getMessage());
      return null;
    }
  }

  public static List<Artifact> extractMavenArtifactsFromContentStream(InputStream contentStream) throws IOException {
    List<Artifact> artifacts = new ArrayList<>();
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

  public static String buildDownloadUrl(Artifact artifact, String version) {
    String groupIdByVersion = artifact.getGroupId();
    String artifactIdByVersion = artifact.getArtifactId();
    String repoUrl = StringUtils.defaultIfBlank(artifact.getRepoUrl(), MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL);
    ArchivedArtifact archivedArtifactBestMatchVersion = findArchivedArtifactInfoBestMatchWithVersion(version,
        artifact.getArchivedArtifacts());

    if (Objects.nonNull(archivedArtifactBestMatchVersion)) {
      groupIdByVersion = archivedArtifactBestMatchVersion.getGroupId();
      artifactIdByVersion = archivedArtifactBestMatchVersion.getArtifactId();
    }
    groupIdByVersion = groupIdByVersion.replace(CommonConstants.DOT_SEPARATOR, CommonConstants.SLASH);
    return buildDownloadUrl(artifactIdByVersion, version, artifact.getType(), repoUrl, groupIdByVersion,
        StringUtils.EMPTY);
  }

  public static String buildDownloadUrl(Metadata metadata, String version) {
    String groupIdByVersion = metadata.getGroupId();
    groupIdByVersion = groupIdByVersion.replace(CommonConstants.DOT_SEPARATOR, CommonConstants.SLASH);
    return buildDownloadUrl(metadata.getArtifactId(), version, metadata.getType(), metadata.getRepoUrl(),
        groupIdByVersion, StringUtils.EMPTY);
  }

  public static String buildDownloadUrl(String artifactId, String baseVersion, String type, String repoUrl,
      String groupId, String version) {
    groupId = groupId.replace(CommonConstants.DOT_SEPARATOR, CommonConstants.SLASH);
    if (StringUtils.isBlank(version)) {
      version = baseVersion;
    }
    String artifactFileName = String.format(MavenConstants.ARTIFACT_FILE_NAME_FORMAT, artifactId, version, type);
    return String.join(CommonConstants.SLASH, repoUrl, groupId, artifactId, baseVersion, artifactFileName);
  }


  public static ArchivedArtifact findArchivedArtifactInfoBestMatchWithVersion(String version,
      List<ArchivedArtifact> archivedArtifacts) {
    if (CollectionUtils.isEmpty(archivedArtifacts)) {
      return null;
    }
    return archivedArtifacts.stream().filter(
        archivedArtifact -> MavenVersionComparator.compare(archivedArtifact.getLastVersion(),
            version) >= 0).findAny().orElse(null);
  }

  public static MavenArtifactModel convertMavenArtifactToModel(Artifact artifact, String version) {
    String artifactName = artifact.getName();
    if (StringUtils.isBlank(artifactName)) {
      artifactName = GitHubUtils.convertArtifactIdToName(artifact.getArtifactId());
    }
    artifact.setType(StringUtils.defaultIfBlank(artifact.getType(), ProductJsonConstants.DEFAULT_PRODUCT_TYPE));
    artifactName = String.format(MavenConstants.ARTIFACT_NAME_FORMAT, artifactName, artifact.getType());
    return MavenArtifactModel.builder().name(artifactName).downloadUrl(buildDownloadUrl(artifact, version)).build();
  }

  public static List<MavenArtifactModel> convertArtifactsToModels(List<Artifact> artifacts, String version) {
    List<MavenArtifactModel> results = new ArrayList<>();
    if (!CollectionUtils.isEmpty(artifacts)) {
      for (Artifact artifact : artifacts) {
        MavenArtifactModel mavenArtifactModel = convertMavenArtifactToModel(artifact, version);
        results.add(mavenArtifactModel);
      }
    }
    return results;
  }

  public static String buildSnapshotMetadataUrlFromArtifactInfo(String repoUrl, String groupId, String artifactId,
      String snapshotVersion) {
    if (StringUtils.isAnyBlank(groupId, artifactId)) {
      return StringUtils.EMPTY;
    }
    repoUrl = Optional.ofNullable(repoUrl).orElse(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL);
    groupId = groupId.replace(CommonConstants.DOT_SEPARATOR, CommonConstants.SLASH);
    return String.join(CommonConstants.SLASH, repoUrl, groupId, artifactId, snapshotVersion,
        MavenConstants.METADATA_URL_POSTFIX);
  }

  public static String buildMetadataUrlFromArtifactInfo(String repoUrl, String groupId, String artifactId) {
    if (StringUtils.isAnyBlank(groupId, artifactId)) {
      return StringUtils.EMPTY;
    }
    repoUrl = Optional.ofNullable(repoUrl).orElse(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL);
    groupId = groupId.replace(CommonConstants.DOT_SEPARATOR, CommonConstants.SLASH);
    return String.join(CommonConstants.SLASH, repoUrl, groupId, artifactId, MavenConstants.METADATA_URL_POSTFIX);
  }

  public static Metadata convertArtifactToMetadata(String productId, Artifact artifact, String metadataUrl) {
    String artifactName = artifact.getName();
    if (StringUtils.isBlank(artifactName)) {
      artifactName = GitHubUtils.convertArtifactIdToName(artifact.getArtifactId());
    }
    String type = StringUtils.defaultIfBlank(artifact.getType(), ProductJsonConstants.DEFAULT_PRODUCT_TYPE);
    artifactName = String.format(MavenConstants.ARTIFACT_NAME_FORMAT, artifactName, type);
    return Metadata.builder().groupId(artifact.getGroupId()).versions(new HashSet<>()).productId(productId).artifactId(
        artifact.getArtifactId()).url(metadataUrl).repoUrl(
        StringUtils.defaultIfEmpty(artifact.getRepoUrl(), MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL)).type(type).name(
        artifactName).isProductArtifact(BooleanUtils.isTrue(artifact.getIsProductArtifact())).build();
  }

  public static Metadata buildSnapShotMetadataFromVersion(Metadata metadata, String version) {
    String snapshotMetadataUrl = buildSnapshotMetadataUrlFromArtifactInfo(metadata.getRepoUrl(), metadata.getGroupId(),
        metadata.getArtifactId(), version);
    return Metadata.builder().url(snapshotMetadataUrl).repoUrl(metadata.getRepoUrl()).groupId(
        metadata.getGroupId()).artifactId(metadata.getArtifactId()).type(metadata.getType()).productId(
        metadata.getProductId()).name(metadata.getName()).isProductArtifact(metadata.isProductArtifact()).build();
  }

  public static MavenArtifactModel buildMavenArtifactModelFromSnapShotMetadata(String version,
      Metadata snapShotMetadata) {
    return new MavenArtifactModel(snapShotMetadata.getName(),
        buildDownloadUrl(snapShotMetadata.getArtifactId(), version, snapShotMetadata.getType(),
            snapShotMetadata.getRepoUrl(), snapShotMetadata.getGroupId(), snapShotMetadata.getSnapshotVersionValue()),
        snapShotMetadata.getArtifactId().contains(snapShotMetadata.getGroupId()));
  }

  public static String getMetadataContentFromUrl(String metadataUrl) {
    try {
      return restTemplate.getForObject(metadataUrl, String.class);
    } catch (Exception e) {
      log.error("**MetadataService: Failed to fetch metadata from url {}", metadataUrl);
      return StringUtils.EMPTY;
    }
  }

  public static Set<Metadata> convertArtifactsToMetadataSet(Set<Artifact> artifacts, String productId) {
    Set<Metadata> results = new HashSet<>();
    if (!CollectionUtils.isEmpty(artifacts)) {
      artifacts.forEach(artifact -> {
        String metadataUrl = buildMetadataUrlFromArtifactInfo(artifact.getRepoUrl(), artifact.getGroupId(),
            artifact.getArtifactId());
        results.add(convertArtifactToMetadata(productId, artifact, metadataUrl));
        extractMetaDataFromArchivedArtifacts(productId, artifact, results);
      });
    }
    return results;
  }

  public static void extractMetaDataFromArchivedArtifacts(String productId, Artifact artifact,
      Set<Metadata> results) {
    if (!CollectionUtils.isEmpty(artifact.getArchivedArtifacts())) {
      artifact.getArchivedArtifacts().forEach(archivedArtifact -> {
        String archivedMetadataUrl = buildMetadataUrlFromArtifactInfo(artifact.getRepoUrl(),
            archivedArtifact.getGroupId(), archivedArtifact.getArtifactId());
        results.add(convertArtifactToMetadata(productId, artifact, archivedMetadataUrl));
      });
    }
  }

  public static List<Artifact> filterNonProductArtifactFromMeta(List<Artifact> artifactsFromMeta) {
    if(CollectionUtils.isEmpty(artifactsFromMeta)) {
      return artifactsFromMeta;
    }
    return artifactsFromMeta.stream()
        .filter(artifact -> !artifact.getArtifactId().endsWith(MavenConstants.PRODUCT_ARTIFACT_POSTFIX)).toList();
  }
}
