package com.axonivy.market.service.impl;

import com.axonivy.market.comparator.ArchivedArtifactsComparator;
import com.axonivy.market.comparator.LatestVersionComparator;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.entity.MavenArtifactModel;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.github.model.ArchivedArtifact;
import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.model.MavenArtifactVersionModel;
import com.axonivy.market.model.VersionAndUrlModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductModuleContentRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.VersionService;
import com.axonivy.market.util.VersionUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHContent;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.axonivy.market.constants.ProductJsonConstants.NAME;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
@Log4j2
@Service
@Getter
public class VersionServiceImpl implements VersionService {

  private final GHAxonIvyProductRepoService gitHubService;
  private final MavenArtifactVersionRepository mavenArtifactVersionRepository;
  private final ProductRepository productRepository;
  private final ProductJsonContentRepository productJsonContentRepository;
  private final ProductModuleContentRepository productModuleContentRepository;
  @Getter
  private String repoName;
  private Map<String, List<ArchivedArtifact>> archivedArtifactsMap;
  private List<MavenArtifact> artifactsFromMeta;
  private MavenArtifactVersion proceedDataCache;
  private MavenArtifact metaProductArtifact;
  private final LatestVersionComparator latestVersionComparator = new LatestVersionComparator();
  @Getter
  private String productJsonFilePath;
  private String productId;
  private final ObjectMapper mapper = new ObjectMapper();

  public VersionServiceImpl(GHAxonIvyProductRepoService gitHubService,
      MavenArtifactVersionRepository mavenArtifactVersionRepository, ProductRepository productRepository,
      ProductJsonContentRepository productJsonContentRepository, ProductModuleContentRepository productModuleContentRepository) {
    this.gitHubService = gitHubService;
    this.mavenArtifactVersionRepository = mavenArtifactVersionRepository;
    this.productRepository = productRepository;

    this.productJsonContentRepository = productJsonContentRepository;
    this.productModuleContentRepository = productModuleContentRepository;
    StringUtils.I
  }

  private void resetData() {
    repoName = null;
    archivedArtifactsMap = new HashMap<>();
    artifactsFromMeta = Collections.emptyList();
    proceedDataCache = null;
    metaProductArtifact = null;
    productJsonFilePath = null;
    productId = null;
  }

  public List<MavenArtifactVersionModel> getArtifactsAndVersionToDisplay(String productId, Boolean isShowDevVersion,
      String designerVersion) {
    List<MavenArtifactVersionModel> results = new ArrayList<>();
    resetData();

    this.productId = productId;
    artifactsFromMeta = getProductMetaArtifacts(productId);
    List<String> versionsToDisplay = VersionUtils.getVersionsToDisplay(getPersistedVersions(productId), isShowDevVersion, designerVersion);
    proceedDataCache = mavenArtifactVersionRepository.findById(productId).orElse(new MavenArtifactVersion(productId));
    metaProductArtifact = artifactsFromMeta.stream()
        .filter(artifact -> artifact.getArtifactId().endsWith(MavenConstants.PRODUCT_ARTIFACT_POSTFIX)).findAny()
        .orElse(new MavenArtifact());

    sanitizeMetaArtifactBeforeHandle();

    boolean isNewVersionDetected = handleArtifactForVersionToDisplay(versionsToDisplay, results);
    if (isNewVersionDetected) {
      mavenArtifactVersionRepository.save(proceedDataCache);
    }
    return results;
  }

  @Override
  public Map<String, Object> getProductJsonContentByIdAndVersion(String productId, String version){
    Map<String, Object> result = new HashMap<>();
    try {
      ProductJsonContent productJsonContent = productJsonContentRepository.findByProductIdAndVersion(productId, version);
      if (ObjectUtils.isEmpty(productJsonContent)) {
        return new HashMap<>();
      }
      result = mapper.readValue(productJsonContent.getContent(), Map.class);
      result.computeIfAbsent(NAME, k -> productJsonContent.getName());

    } catch (JsonProcessingException jsonProcessingException){
      log.error(jsonProcessingException.getMessage());
    }
    return result;
  }

  @Override
  public List<VersionAndUrlModel> getVersionsForDesigner(String productId) {
    List<VersionAndUrlModel> versionAndUrlList = new ArrayList<>();
    List<String> versions = productRepository.getReleasedVersionsById(productId);
    for (String version : versions) {
      Link link = linkTo(
          methodOn(ProductDetailsController.class).findProductJsonContent(productId, version)).withSelfRel();
      VersionAndUrlModel versionAndUrlModel = new VersionAndUrlModel(version, link.getHref());
      versionAndUrlList.add(versionAndUrlModel);
    }
    return versionAndUrlList;
  }

  public boolean handleArtifactForVersionToDisplay(List<String> versionsToDisplay,
      List<MavenArtifactVersionModel> result) {
    boolean isNewVersionDetected = false;
    for (String version : versionsToDisplay) {
      List<MavenArtifactModel> artifactsInVersion = convertMavenArtifactsToModels(artifactsFromMeta, version);
      List<MavenArtifactModel> productArtifactModels = proceedDataCache.getProductArtifactWithVersionReleased()
          .get(version);
      if (productArtifactModels == null) {
        isNewVersionDetected = true;
        productArtifactModels = updateArtifactsInVersionWithProductArtifact(version);
      }
      artifactsInVersion.addAll(productArtifactModels);
      result.add(new MavenArtifactVersionModel(version, artifactsInVersion.stream().distinct().toList()));
    }
    return isNewVersionDetected;
  }

  public List<MavenArtifactModel> updateArtifactsInVersionWithProductArtifact(String version) {
    List<MavenArtifactModel> productArtifactModels = convertMavenArtifactsToModels(getMavenArtifactsFromProductJsonByVersion(version),
        version);
    proceedDataCache.getVersions().add(version);
    proceedDataCache.getProductArtifactWithVersionReleased().put(version, productArtifactModels);
    return productArtifactModels;
  }

  public List<MavenArtifact> getProductMetaArtifacts(String productId) {
    Product productInfo = productRepository.findById(productId).orElse(new Product());
    String fullRepoName = productInfo.getRepositoryName();
    if (StringUtils.isNotEmpty(fullRepoName)) {
      repoName = getRepoNameFromMarketRepo(fullRepoName);
    }
    return Optional.ofNullable(productInfo.getArtifacts()).orElse(new ArrayList<>());
  }

  public void sanitizeMetaArtifactBeforeHandle() {
    artifactsFromMeta.remove(metaProductArtifact);
    artifactsFromMeta.forEach(artifact -> {
      List<ArchivedArtifact> archivedArtifacts = new ArrayList<>(
          Optional.ofNullable(artifact.getArchivedArtifacts()).orElse(Collections.emptyList()).stream()
              .sorted(new ArchivedArtifactsComparator()).toList());
      Collections.reverse(archivedArtifacts);
      archivedArtifactsMap.put(artifact.getArtifactId(), archivedArtifacts);
    });
  }

  public List<String> getPersistedVersions(String productId) {
    var product = productRepository.findById(productId);
    Set<String> versions = new HashSet<>();
    if (product.isPresent()) {
      versions.addAll(product.get().getReleasedVersions());
    }
    if (CollectionUtils.isEmpty(versions)) {
      versions.addAll(productModuleContentRepository.findTagsByProductId(productId));
      versions = versions.stream().map(VersionUtils::convertTagToVersion).collect(Collectors.toSet());
    }
    return new ArrayList<>(versions);
  }

  @Override
  public String buildMavenMetadataUrlFromArtifact(String repoUrl, String groupId, String artifactID) {
    if (StringUtils.isAnyBlank(groupId, artifactID)) {
      return StringUtils.EMPTY;
    }
    repoUrl = Optional.ofNullable(repoUrl).orElse(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL);
    groupId = groupId.replace(CommonConstants.DOT_SEPARATOR, CommonConstants.SLASH);
    return String.format(MavenConstants.METADATA_URL_FORMAT, repoUrl, groupId, artifactID);
  }

  public List<MavenArtifact> getMavenArtifactsFromProductJsonByVersion(String version) {
    ProductJsonContent productJson = productJsonContentRepository.findByProductIdAndVersion(productId, version);
    if (Objects.isNull(productJson) || StringUtils.isBlank(productJson.getContent())) {
      return new ArrayList<>();
    }
    InputStream contentStream = IOUtils.toInputStream(productJson.getContent(), StandardCharsets.UTF_8);
    try {
      return gitHubService.extractMavenArtifactsFromContentStream(contentStream);
    } catch (IOException e) {
      log.error("Can not get maven artifacts from Product.json of {} - version {}:{}", productId, version, e.getMessage());
      return new ArrayList<>();
    }
  }

  public MavenArtifactModel convertMavenArtifactToModel(MavenArtifact artifact, String version) {
    String artifactName = artifact.getName();
    if (StringUtils.isBlank(artifactName)) {
      artifactName = GitHubUtils.convertArtifactIdToName(artifact.getArtifactId());
    }
    artifact.setType(Optional.ofNullable(artifact.getType()).orElse("iar"));
    artifactName = String.format(MavenConstants.ARTIFACT_NAME_FORMAT, artifactName, artifact.getType());
    return new MavenArtifactModel(artifactName, buildDownloadUrlFromArtifactAndVersion(artifact, version),
        artifact.getIsProductArtifact());
  }

  public List<MavenArtifactModel> convertMavenArtifactsToModels(List<MavenArtifact> artifacts, String version) {
    List<MavenArtifactModel> results = new ArrayList<>();
    if (!CollectionUtils.isEmpty(artifacts)) {
      for (MavenArtifact artifact : artifacts) {
        MavenArtifactModel mavenArtifactModel = convertMavenArtifactToModel(artifact, version);
        results.add(mavenArtifactModel);
      }
    }
    return results;
  }

  public String buildDownloadUrlFromArtifactAndVersion(MavenArtifact artifact, String version) {
    String groupIdByVersion = artifact.getGroupId();
    String artifactIdByVersion = artifact.getArtifactId();
    String repoUrl = Optional.ofNullable(artifact.getRepoUrl()).orElse(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL);
    ArchivedArtifact archivedArtifactBestMatchVersion = findArchivedArtifactInfoBestMatchWithVersion(
        artifact.getArtifactId(), version);

    if (Objects.nonNull(archivedArtifactBestMatchVersion)) {
      groupIdByVersion = archivedArtifactBestMatchVersion.getGroupId();
      artifactIdByVersion = archivedArtifactBestMatchVersion.getArtifactId();
    }
    groupIdByVersion = groupIdByVersion.replace(CommonConstants.DOT_SEPARATOR, CommonConstants.SLASH);
    return String.format(MavenConstants.ARTIFACT_DOWNLOAD_URL_FORMAT, repoUrl, groupIdByVersion, artifactIdByVersion,
        version, artifactIdByVersion, version, artifact.getType());
  }

  public ArchivedArtifact findArchivedArtifactInfoBestMatchWithVersion(String artifactId, String version) {
    List<ArchivedArtifact> archivedArtifacts = archivedArtifactsMap.get(artifactId);

    if (CollectionUtils.isEmpty(archivedArtifacts)) {
      return null;
    }
    for (ArchivedArtifact archivedArtifact : archivedArtifacts) {
      if (latestVersionComparator.compare(archivedArtifact.getLastVersion(), version) <= 0) {
        return archivedArtifact;
      }
    }
    return null;
  }

  public String getRepoNameFromMarketRepo(String fullRepoName) {
    String[] repoNamePart = fullRepoName.split("/");
    return repoNamePart[repoNamePart.length - 1];
  }
}
