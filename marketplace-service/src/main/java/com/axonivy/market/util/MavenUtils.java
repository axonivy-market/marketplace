package com.axonivy.market.util;

import com.axonivy.market.bo.ArchivedArtifact;
import com.axonivy.market.bo.Artifact;
import com.axonivy.market.comparator.MavenVersionComparator;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.model.MavenArtifactModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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

import static com.axonivy.market.constants.MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MavenUtils {
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final RestTemplate restTemplate = new RestTemplate();

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

    // Not convert to artifact if id of node is not maven-import or maven-dependency
    List<String> installerIdsToDisplay = List.of(ProductJsonConstants.MAVEN_DEPENDENCY_INSTALLER_ID,
        ProductJsonConstants.MAVEN_IMPORT_INSTALLER_ID, ProductJsonConstants.MAVEN_DROPINS_INSTALLER_ID);

    for (JsonNode mavenNode : installersNode) {
      JsonNode dataNode = mavenNode.path(ProductJsonConstants.DATA);
      if (!installerIdsToDisplay.contains(mavenNode.path(ProductJsonConstants.ID).asText())) {
        continue;
      }

      // Extract repository URL
      JsonNode repositoriesNode = dataNode.path(ProductJsonConstants.REPOSITORIES);
      String repoUrl = Optional.of(repositoriesNode)
          .filter(jsonNode -> !jsonNode.isMissingNode())
          .map(jsonNode -> jsonNode.get(0))
          .map(jsonNode -> jsonNode.get(ProductJsonConstants.URL))
          .map(JsonNode::asText)
          .orElse(DEFAULT_IVY_MAVEN_BASE_URL);

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
    String repoUrl = StringUtils.defaultIfBlank(artifact.getRepoUrl(), DEFAULT_IVY_MAVEN_BASE_URL);
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

  //TODO: Snapshot
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
    return MavenArtifactModel.builder().name(artifactName).downloadUrl(buildDownloadUrl(artifact, version)).artifactId(artifact.getArtifactId()).build();
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
    repoUrl = StringUtils.defaultIfEmpty(repoUrl, DEFAULT_IVY_MAVEN_BASE_URL);
    groupId = groupId.replace(CommonConstants.DOT_SEPARATOR, CommonConstants.SLASH);
    return String.join(CommonConstants.SLASH, repoUrl, groupId, artifactId, snapshotVersion,
        MavenConstants.METADATA_URL_POSTFIX);
  }

  public static String buildMetadataUrlFromArtifactInfo(String repoUrl, String groupId, String artifactId) {
    if (StringUtils.isAnyBlank(groupId, artifactId)) {
      return StringUtils.EMPTY;
    }
    repoUrl = StringUtils.defaultIfEmpty(repoUrl, DEFAULT_IVY_MAVEN_BASE_URL);
    groupId = groupId.replace(CommonConstants.DOT_SEPARATOR, CommonConstants.SLASH);
    return String.join(CommonConstants.SLASH, repoUrl, groupId, artifactId, MavenConstants.METADATA_URL_POSTFIX);
  }

  public static Metadata convertArtifactToMetadata(String productId, Artifact artifact, String metadataUrl) {
    return convertArtifactToMetadata(productId, artifact, metadataUrl, null);
  }

  public static Metadata convertArtifactToMetadata(String productId, Artifact artifact, String metadataUrl,
      ArchivedArtifact archivedArtifact) {
    String artifactName = StringUtils.defaultIfBlank(artifact.getName(),
        GitHubUtils.convertArtifactIdToName(artifact.getArtifactId()));
    String artifactId = Objects.isNull(archivedArtifact) ? artifact.getArtifactId() : archivedArtifact.getArtifactId();
    String groupId = Objects.isNull(archivedArtifact) ? artifact.getGroupId() : archivedArtifact.getGroupId();
    String type = StringUtils.defaultIfBlank(artifact.getType(), ProductJsonConstants.DEFAULT_PRODUCT_TYPE);
    String repoUrl = StringUtils.defaultIfEmpty(artifact.getRepoUrl(), DEFAULT_IVY_MAVEN_BASE_URL);
    artifactName = String.format(MavenConstants.ARTIFACT_NAME_FORMAT, artifactName, type);

    return Metadata.builder().groupId(groupId).versions(new HashSet<>()).productId(productId).artifactId(
        artifactId).url(metadataUrl).repoUrl(repoUrl).type(type).name(artifactName).isProductArtifact(
        BooleanUtils.isTrue(artifact.getIsProductArtifact())).build();
  }

  public static Metadata buildSnapShotMetadataFromVersion(Metadata metadata, String version) {
    String snapshotMetadataUrl = buildSnapshotMetadataUrlFromArtifactInfo(metadata.getRepoUrl(), metadata.getGroupId(),
        metadata.getArtifactId(), version);
    return Metadata.builder().url(snapshotMetadataUrl).repoUrl(metadata.getRepoUrl()).groupId(
        metadata.getGroupId()).artifactId(metadata.getArtifactId()).type(metadata.getType()).productId(
        metadata.getProductId()).name(metadata.getName()).isProductArtifact(metadata.isProductArtifact()).build();
  }

  public static MavenArtifactModel buildMavenArtifactModelFromMetadata(String version, Metadata metadata) {
    String downloadUrl = buildDownloadUrl(metadata.getArtifactId(), version, metadata.getType(), metadata.getRepoUrl(),
        metadata.getGroupId(), metadata.getSnapshotVersionValue());
    return MavenArtifactModel.builder().name(metadata.getName()).downloadUrl(downloadUrl).isInvalidArtifact(
        metadata.getArtifactId().contains(metadata.getGroupId())).artifactId(metadata.getArtifactId()).build();
  }

  public static String getMetadataContentFromUrl(String metadataUrl) {
    try {
      return restTemplate.getForObject(metadataUrl, String.class);
    } catch (Exception e) {
      log.error("**MetadataService: Failed to fetch metadata from url {}", metadataUrl);
      return StringUtils.EMPTY;
    }
  }

  public static boolean isProductArtifactId(String artifactId) {
    return StringUtils.endsWith(artifactId, MavenConstants.PRODUCT_ARTIFACT_POSTFIX);
  }

  public static Set<Metadata> convertArtifactsToMetadataSet(Set<Artifact> artifacts, String productId) {
    Set<Metadata> results = new HashSet<>();
    if (!CollectionUtils.isEmpty(artifacts)) {
      artifacts.forEach(artifact -> {
        String metadataUrl = buildMetadataUrlFromArtifactInfo(artifact.getRepoUrl(), artifact.getGroupId(),
            artifact.getArtifactId());
        results.add(convertArtifactToMetadata(productId, artifact, metadataUrl));
        results.addAll(extractMetaDataFromArchivedArtifacts(productId, artifact));
      });
    }
    return results;
  }

  public static Set<Metadata> extractMetaDataFromArchivedArtifacts(String productId, Artifact artifact) {
    Set<Metadata> results = new HashSet<>();
    if (!CollectionUtils.isEmpty(artifact.getArchivedArtifacts())) {
      artifact.getArchivedArtifacts().forEach(archivedArtifact -> {
        String archivedMetadataUrl = buildMetadataUrlFromArtifactInfo(artifact.getRepoUrl(),
            archivedArtifact.getGroupId(), archivedArtifact.getArtifactId());
        results.add(convertArtifactToMetadata(productId, artifact, archivedMetadataUrl, archivedArtifact));
      });
    }
    return results;
  }

  public static List<Artifact> filterNonProductArtifactFromList(List<Artifact> artifactsFromMeta) {
    if (CollectionUtils.isEmpty(artifactsFromMeta)) {
      return artifactsFromMeta;
    }
    return artifactsFromMeta.stream().filter(
        artifact -> !artifact.getArtifactId().endsWith(MavenConstants.PRODUCT_ARTIFACT_POSTFIX)).toList();
  }

  public static List<String> getAllExistingVersions(MavenArtifactVersion existingMavenArtifactVersion,
      boolean isShowDevVersion, String designerVersion) {
    Set<String> existingProductsArtifactByVersion =
        new HashSet<>(existingMavenArtifactVersion.getProductArtifactsByVersion().keySet());
    Set<String> existingAdditionalArtifactByVersion =
        existingMavenArtifactVersion.getProductArtifactsByVersion().keySet();
    existingProductsArtifactByVersion.addAll(existingAdditionalArtifactByVersion);
    return VersionUtils.getVersionsToDisplay(new ArrayList<>(existingProductsArtifactByVersion), isShowDevVersion,
        designerVersion);
  }
}
