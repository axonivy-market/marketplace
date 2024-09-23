package com.axonivy.market.service.impl;

import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.entity.MavenArtifactModel;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.factory.MavenArtifactFactory;
import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.model.MavenArtifactVersionModel;
import com.axonivy.market.model.VersionAndUrlModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductModuleContentRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.service.VersionService;
import com.axonivy.market.util.VersionUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.axonivy.market.constants.ProductJsonConstants.NAME;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Log4j2
@Service
public class VersionServiceImpl implements VersionService {

  private final GHAxonIvyProductRepoService gitHubService;
  private final FileDownloadService fileDownloadService;
  private final MavenArtifactVersionRepository mavenArtifactVersionRepository;
  private final ProductRepository productRepo;
  private final ProductJsonContentRepository productJsonRepo;
  private final ProductModuleContentRepository productContentRepo;
  private final ObjectMapper mapper = new ObjectMapper();

  public VersionServiceImpl(GHAxonIvyProductRepoService gitHubService, FileDownloadService fileDownloadService,
      MavenArtifactVersionRepository mavenArtifactVersionRepository, ProductRepository productRepo,
      ProductJsonContentRepository productJsonRepo, ProductModuleContentRepository productContentRepo) {
    this.gitHubService = gitHubService;
    this.fileDownloadService = fileDownloadService;
    this.mavenArtifactVersionRepository = mavenArtifactVersionRepository;
    this.productRepo = productRepo;
    this.productJsonRepo = productJsonRepo;
    this.productContentRepo = productContentRepo;
  }

  public List<MavenArtifactVersionModel> getArtifactsAndVersionToDisplay(String productId, Boolean isShowDevVersion,
      String designerVersion) {
    List<String> versionsToDisplay = VersionUtils.getVersionsToDisplay(getPersistedVersions(productId),
        isShowDevVersion, designerVersion);
    List<MavenArtifact> artifactsFromMeta = getArtifactsFromMeta(productId);
    MavenArtifact productArtifact = artifactsFromMeta.stream().filter(
        artifact -> artifact.getArtifactId().endsWith(MavenConstants.PRODUCT_ARTIFACT_POSTFIX)).findAny().orElse(
        new MavenArtifact());
    artifactsFromMeta.remove(productArtifact);
    return handleArtifactForVersionToDisplay(versionsToDisplay, productId, artifactsFromMeta);
  }

  private void handleDocumentForPortalGuide(List<MavenArtifactVersionModel> mavenArtifactVersionModels) {
    if (ObjectUtils.isEmpty(mavenArtifactVersionModels)) {
      return;
    }
    for (var artifactVersionModel : mavenArtifactVersionModels) {
      for (var version : artifactVersionModel.getArtifactsByVersion()) {
        if (version.getDownloadUrl().contains("portal-guide")) {
          try {
            fileDownloadService.downloadAndUnzipFile(version.getDownloadUrl(), false);
          } catch (Exception e) {
            log.warn("Cannot download portal-guide for {}", version.getDownloadUrl());
          }
        }
      }
    }
  }

  @Override
  public Map<String, Object> getProductJsonContentByIdAndVersion(String productId, String version) {
    Map<String, Object> result = new HashMap<>();
    try {
      ProductJsonContent productJsonContent = productJsonRepo.findByProductIdAndVersion(productId, version);
      if (ObjectUtils.isEmpty(productJsonContent)) {
        return new HashMap<>();
      }
      result = mapper.readValue(productJsonContent.getContent(), Map.class);
      result.computeIfAbsent(NAME, k -> productJsonContent.getName());

    } catch (JsonProcessingException jsonProcessingException) {
      log.error(jsonProcessingException.getMessage());
    }
    return result;
  }

  @Override
  public List<VersionAndUrlModel> getVersionsForDesigner(String productId) {
    List<VersionAndUrlModel> versionAndUrlList = new ArrayList<>();
    List<String> versions = productRepo.getReleasedVersionsById(productId);
    for (String version : versions) {
      Link link = linkTo(
          methodOn(ProductDetailsController.class).findProductJsonContent(productId, version)).withSelfRel();
      VersionAndUrlModel versionAndUrlModel = new VersionAndUrlModel(version, link.getHref());
      versionAndUrlList.add(versionAndUrlModel);
    }
    return versionAndUrlList;
  }

  /**
   * This function will combine default artifacts (from product.json) and custom artifacts from (meta.json)
   * of each version and return it to user.
   * By default, all artifacts model (from product.json) by version to display will be taken from db.
   * If new version is detected, new model will be built and save back to db.
   **/
  public List<MavenArtifactVersionModel> handleArtifactForVersionToDisplay(List<String> versionsToDisplay,
      String productId, List<MavenArtifact> artifactsFromMeta) {
    boolean isNewVersionDetected = false;
    List<MavenArtifactVersionModel> results = new ArrayList<>();
    MavenArtifactVersion cache = mavenArtifactVersionRepository.findById(productId).orElse(
        new MavenArtifactVersion(productId));
    for (String version : versionsToDisplay) {
      List<MavenArtifactModel> artifactsInVersion = convertArtifactsToModels(artifactsFromMeta, version);
      List<MavenArtifactModel> productArtifactModels = cache.getProductArtifactWithVersionReleased().get(version);
      if (productArtifactModels == null) {
        isNewVersionDetected = true;
        productArtifactModels = convertArtifactsToModels(getMavenArtifactsFromProductJsonByVersion(version, productId),
            version);
        cache.getProductArtifactWithVersionReleased().put(version, productArtifactModels);
      }
      artifactsInVersion.addAll(productArtifactModels);
      results.add(new MavenArtifactVersionModel(version, artifactsInVersion.stream().distinct().toList()));
    }
    if (isNewVersionDetected) {
      mavenArtifactVersionRepository.save(cache);
    }
    return results;
  }

  public List<MavenArtifact> getArtifactsFromMeta(String productId) {
    Product productInfo = productRepo.findById(productId).orElse(new Product());
    return Optional.ofNullable(productInfo.getArtifacts()).orElse(new ArrayList<>());
  }

  public List<String> getPersistedVersions(String productId) {
    var product = productRepo.findById(productId);
    Set<String> versions = new HashSet<>();
    if (product.isPresent()) {
      versions.addAll(product.get().getReleasedVersions());
    }
    if (CollectionUtils.isEmpty(versions)) {
      versions.addAll(productContentRepo.findTagsByProductId(productId));
      versions = versions.stream().map(VersionUtils::convertTagToVersion).collect(Collectors.toSet());
    }
    return new ArrayList<>(versions);
  }

  public List<MavenArtifact> getMavenArtifactsFromProductJsonByVersion(String version, String productId) {
    ProductJsonContent productJson = productJsonRepo.findByProductIdAndVersion(productId, version);
    if (Objects.isNull(productJson) || StringUtils.isBlank(productJson.getContent())) {
      return new ArrayList<>();
    }
    InputStream contentStream = IOUtils.toInputStream(productJson.getContent(), StandardCharsets.UTF_8);
    try {
      return gitHubService.extractMavenArtifactsFromContentStream(contentStream);
    } catch (IOException e) {
      log.error("Can not get maven artifacts from Product.json of {} - version {}:{}", productId, version,
          e.getMessage());
      return new ArrayList<>();
    }
  }

  public MavenArtifactModel convertMavenArtifactToModel(MavenArtifact artifact, String version) {
    String artifactName = artifact.getName();
    if (StringUtils.isBlank(artifactName)) {
      artifactName = GitHubUtils.convertArtifactIdToName(artifact.getArtifactId());
    }
    artifact.setType(StringUtils.defaultIfBlank(artifact.getType(), ProductJsonConstants.DEFAULT_PRODUCT_TYPE));
    artifactName = String.format(MavenConstants.ARTIFACT_NAME_FORMAT, artifactName, artifact.getType());
    return new MavenArtifactModel(artifactName,
        MavenArtifactFactory.buildDownloadUrlByVersion(artifact, version),
        artifact.getIsProductArtifact());
  }

  public List<MavenArtifactModel> convertArtifactsToModels(List<MavenArtifact> artifacts, String version) {
    List<MavenArtifactModel> results = new ArrayList<>();
    if (!CollectionUtils.isEmpty(artifacts)) {
      for (MavenArtifact artifact : artifacts) {
        MavenArtifactModel mavenArtifactModel = convertMavenArtifactToModel(artifact, version);
        results.add(mavenArtifactModel);
      }
    }
    return results;
  }
}
