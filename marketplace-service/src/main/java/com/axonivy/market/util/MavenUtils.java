package com.axonivy.market.util;

import com.axonivy.market.comparator.MavenVersionComparator;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.entity.ArchivedArtifact;
import com.axonivy.market.entity.Artifact;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.entity.key.MavenArtifactKey;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.axonivy.market.constants.MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL;
import static com.axonivy.market.constants.MavenConstants.DEFAULT_IVY_MIRROR_MAVEN_BASE_URL;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MavenUtils {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static List<Artifact> getMavenArtifactsFromProductJson(ProductJsonContent productJson) {
    if (Objects.isNull(productJson) || StringUtils.isBlank(productJson.getContent())) {
      return new ArrayList<>();
    }
    InputStream contentStream = IOUtils.toInputStream(productJson.getContent(), StandardCharsets.UTF_8);
    try {
      return extractMavenArtifactsFromContentStream(contentStream);
    } catch (IOException e) {
      log.error(e);
      log.error("Can not get maven artifacts from Product.json of {}", productJson);
      return new ArrayList<>();
    }
  }

  public static Artifact createArtifactFromJsonNode(JsonNode node, String repoUrl, boolean isDependency) {
    var artifact = new Artifact();
    artifact.setRepoUrl(repoUrl);
    artifact.setIsDependency(isDependency);
    artifact.setGroupId(getTextValueFromNodeAndPath(node, ProductJsonConstants.GROUP_ID));
    artifact.setArtifactId(getTextValueFromNodeAndPath(node, ProductJsonConstants.ARTIFACT_ID));
    artifact.setType(getTextValueFromNodeAndPath(node, ProductJsonConstants.TYPE));
    artifact.setIsProductArtifact(true);
    return artifact;
  }

  private static String getTextValueFromNodeAndPath(JsonNode node, String path) {
    if (Objects.isNull(node.path(path))) {
      return StringUtils.EMPTY;
    }
    return node.path(path).asText();
  }

  public static void extractMavenArtifactFromJsonNode(JsonNode dataNode, boolean isDependency,
      Collection<Artifact> artifacts,
      String repoUrl) {
    String nodeName = ProductJsonConstants.PROJECTS;
    if (isDependency) {
      nodeName = ProductJsonConstants.DEPENDENCIES;
    }
    JsonNode dependenciesNode = dataNode.path(nodeName);
    for (JsonNode dependencyNode : dependenciesNode) {
      var artifact = createArtifactFromJsonNode(dependencyNode, repoUrl, isDependency);
      artifacts.add(artifact);
    }
  }

  public static List<Artifact> convertProductJsonToMavenProductInfo(Path folderPath) throws IOException {
    var productJsonPath = folderPath.resolve(ProductJsonConstants.PRODUCT_JSON_FILE);

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

  public static String extractProductJsonContent(Path filePath) {
    try (InputStream contentStream = extractedContentStream(filePath)) {
      return IOUtils.toString(Objects.requireNonNull(contentStream), StandardCharsets.UTF_8);
    } catch (Exception e) {
      log.error(e);
      log.error("Cannot extract product.json file {}", filePath);
      return null;
    }
  }

  public static String buildDownloadUrl(Artifact artifact, String version) {
    String groupIdByVersion = artifact.getGroupId();
    String artifactIdByVersion = artifact.getArtifactId();
    String repoUrl = StringUtils.defaultIfBlank(artifact.getRepoUrl(), DEFAULT_IVY_MAVEN_BASE_URL);
    var archivedArtifactBestMatchVersion = findArchivedArtifactInfoBestMatchWithVersion(version,
        artifact.getArchivedArtifacts());

    if (Objects.nonNull(archivedArtifactBestMatchVersion)) {
      groupIdByVersion = archivedArtifactBestMatchVersion.getGroupId();
      artifactIdByVersion = archivedArtifactBestMatchVersion.getArtifactId();
    }
    groupIdByVersion = groupIdByVersion.replace(CommonConstants.DOT_SEPARATOR, CommonConstants.SLASH);
    return buildDownloadUrl(artifactIdByVersion, version, artifact.getType(), repoUrl, groupIdByVersion,
        StringUtils.EMPTY);
  }

  public static String buildDownloadUrl(String artifactId, String baseVersion, String type, String repoUrl,
      String groupId, String version) {
    groupId = groupId.replace(CommonConstants.DOT_SEPARATOR, CommonConstants.SLASH);
    if (StringUtils.isBlank(version)) {
      version = baseVersion;
    }
    var artifactFileName = String.format(MavenConstants.ARTIFACT_FILE_NAME_FORMAT, artifactId, version, type);
    return String.join(CommonConstants.SLASH, repoUrl, groupId, artifactId, baseVersion, artifactFileName);
  }

  public static ArchivedArtifact findArchivedArtifactInfoBestMatchWithVersion(String version,
      Set<ArchivedArtifact> archivedArtifacts) {
    if (CollectionUtils.isEmpty(archivedArtifacts)) {
      return null;
    }
    return archivedArtifacts.stream()
        .filter(Objects::nonNull)
        .sorted((artifact1, artifact2) -> StringUtils.compare(artifact1.getLastVersion(), artifact2.getLastVersion()))
        .filter(archivedArtifact -> MavenVersionComparator.compare(archivedArtifact.getLastVersion(), version) >= 0)
        .findAny().orElse(null);
  }

  public static String buildSnapshotMetadataUrlFromArtifactInfo(String repoUrl, String groupId, String artifactId,
      String snapshotVersion) {
    if (StringUtils.isAnyBlank(groupId, artifactId)) {
      return StringUtils.EMPTY;
    }
    repoUrl = getDefaultMirrorMavenRepo(repoUrl);
    groupId = groupId.replace(CommonConstants.DOT_SEPARATOR, CommonConstants.SLASH);
    return String.join(CommonConstants.SLASH, repoUrl, groupId, artifactId, snapshotVersion,
        MavenConstants.METADATA_URL_POSTFIX);
  }

  public static String buildMetadataUrlFromArtifactInfo(String repoUrl, String groupId, String artifactId) {
    if (StringUtils.isAnyBlank(groupId, artifactId)) {
      return StringUtils.EMPTY;
    }
    repoUrl = getDefaultMirrorMavenRepo(repoUrl);
    groupId = groupId.replace(CommonConstants.DOT_SEPARATOR, CommonConstants.SLASH);
    return String.join(CommonConstants.SLASH, repoUrl, groupId, artifactId, MavenConstants.METADATA_URL_POSTFIX);
  }

  public static Metadata convertArtifactToMetadata(String productId, Artifact artifact, String metadataUrl) {
    return convertArtifactToMetadata(productId, artifact, metadataUrl, null);
  }

  public static Metadata convertArtifactToMetadata(String productId, Artifact artifact, String metadataUrl,
      ArchivedArtifact archivedArtifact) {
    String artifactName = StringUtils.defaultIfBlank(artifact.getName(),
        convertArtifactIdToName(artifact.getArtifactId()));
    String artifactId;
    String groupId;

    if (Objects.isNull(archivedArtifact)) {
      artifactId = artifact.getArtifactId();
      groupId = artifact.getGroupId();
    } else {
      artifactId = archivedArtifact.getArtifactId();
      groupId = archivedArtifact.getGroupId();
    }

    String type = StringUtils.defaultIfBlank(artifact.getType(), ProductJsonConstants.DEFAULT_PRODUCT_TYPE);
    String repoUrl = StringUtils.defaultIfEmpty(artifact.getRepoUrl(), DEFAULT_IVY_MAVEN_BASE_URL);
    artifactName = String.format(MavenConstants.ARTIFACT_NAME_FORMAT, artifactName, type);

    return Metadata.builder().groupId(groupId).versions(new HashSet<>()).productId(productId).artifactId(
        artifactId).url(metadataUrl).repoUrl(repoUrl).type(type).name(artifactName).isProductArtifact(
        BooleanUtils.isTrue(artifact.getIsProductArtifact())).build();
  }

  public static String convertArtifactIdToName(String artifactId) {
    if (StringUtils.isBlank(artifactId)) {
      return StringUtils.EMPTY;
    }
    return Arrays.stream(artifactId.split(CommonConstants.DASH_SEPARATOR))
        .map(part -> part.substring(0, 1).toUpperCase(Locale.getDefault()) + part.substring(1).toLowerCase(
            Locale.getDefault()))
        .collect(Collectors.joining(CommonConstants.SPACE_SEPARATOR));
  }

  public static Metadata buildSnapShotMetadataFromVersion(Metadata metadata, String version) {
    String snapshotMetadataUrl = buildSnapshotMetadataUrlFromArtifactInfo(metadata.getRepoUrl(), metadata.getGroupId(),
        metadata.getArtifactId(), version);
    return Metadata.builder().url(snapshotMetadataUrl).repoUrl(metadata.getRepoUrl()).groupId(
        metadata.getGroupId()).artifactId(metadata.getArtifactId()).type(metadata.getType()).productId(
        metadata.getProductId()).name(metadata.getName()).isProductArtifact(metadata.isProductArtifact()).build();
  }

  public static MavenArtifactVersion buildMavenArtifactVersionFromMetadata(String version, Metadata metadata) {
    String downloadUrl = buildDownloadUrl(metadata.getArtifactId(), version, metadata.getType(), metadata.getRepoUrl(),
        metadata.getGroupId(), metadata.getSnapshotVersionValue());

    MavenArtifactKey mavenArtifactKey = MavenArtifactKey.builder()
        .artifactId(metadata.getArtifactId())
        .productVersion(version)
        .isAdditionalVersion(!metadata.isProductArtifact())
        .build();

    return MavenArtifactVersion.builder()
        .id(mavenArtifactKey)
        .name(metadata.getName())
        .downloadUrl(downloadUrl)
        .isInvalidArtifact(metadata.getArtifactId().contains(metadata.getGroupId()))
        .groupId(metadata.getGroupId())
        .productId(metadata.getProductId())
        .build();
  }

  public static boolean isProductArtifactId(String artifactId) {
    return StringUtils.endsWith(artifactId, MavenConstants.PRODUCT_ARTIFACT_POSTFIX);
  }

  public static Set<Metadata> convertArtifactsToMetadataSet(Set<Artifact> artifacts, String productId) {
    Set<Metadata> results = new HashSet<>();
    if (!CollectionUtils.isEmpty(artifacts)) {
      artifacts.forEach((Artifact artifact) -> {
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
      artifact.getArchivedArtifacts().forEach((ArchivedArtifact archivedArtifact) -> {
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

  public static boolean isProductMetadata(Metadata metadata) {
    return StringUtils.endsWith(Objects.requireNonNullElse(metadata, new Metadata()).getArtifactId(),
        MavenConstants.PRODUCT_ARTIFACT_POSTFIX);
  }

  public static boolean isJsonContentContainOnlyMavenDropins(String jsonContent) {
    return jsonContent.contains(ProductJsonConstants.MAVEN_DROPINS_INSTALLER_ID) && !jsonContent.contains(
        ProductJsonConstants.MAVEN_IMPORT_INSTALLER_ID) && !jsonContent.contains(
        ProductJsonConstants.MAVEN_DEPENDENCY_INSTALLER_ID);
  }

  public static final String getDefaultMirrorMavenRepo(String repoUrl) {
    if (StringUtils.isBlank(repoUrl) || StringUtils.equals(DEFAULT_IVY_MAVEN_BASE_URL, repoUrl)) {
      return DEFAULT_IVY_MIRROR_MAVEN_BASE_URL;
    }
    return repoUrl;
  }
}
