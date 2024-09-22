package com.axonivy.market.service.impl;

import com.axonivy.market.comparator.ArchivedArtifactsComparator;
import com.axonivy.market.comparator.MavenVersionComparator;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.constants.ProductJsonConstants;
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
public class VersionServiceImpl implements VersionService {

  private final GHAxonIvyProductRepoService gitHubService;
  private final MavenArtifactVersionRepository mavenArtifactVersionRepository;
  private final ProductRepository productRepo;
  private final ProductJsonContentRepository productJsonRepo;
  private final ProductModuleContentRepository productContentRepo;
  private final ObjectMapper mapper = new ObjectMapper();

  public VersionServiceImpl(GHAxonIvyProductRepoService gitHubService,
      MavenArtifactVersionRepository mavenArtifactVersionRepository, ProductRepository productRepo,
      ProductJsonContentRepository productJsonRepo, ProductModuleContentRepository productContentRepo) {
    this.gitHubService = gitHubService;
    this.mavenArtifactVersionRepository = mavenArtifactVersionRepository;
    this.productRepo = productRepo;
    this.productJsonRepo = productJsonRepo;
    this.productContentRepo = productContentRepo;
  }

  public static Map<String, List<ArchivedArtifact>> getArchivedArtifactMapFromMeta(
      List<MavenArtifact> artifactsFromMeta) {
    Map<String, List<ArchivedArtifact>> result = new HashMap<>();
    artifactsFromMeta.forEach(artifact -> {
      List<ArchivedArtifact> archivedArtifacts = new ArrayList<>(
          Optional.ofNullable(artifact.getArchivedArtifacts()).orElse(Collections.emptyList()).stream()
              .sorted(new ArchivedArtifactsComparator()).toList());
      Collections.reverse(archivedArtifacts);
      result.put(artifact.getArtifactId(), archivedArtifacts);
    });
    return result;
  }

  public List<MavenArtifactVersionModel> getArtifactsAndVersionToDisplay(String productId, Boolean isShowDevVersion,
      String designerVersion) {
    Map<String, List<MavenArtifactModel>> cache = mavenArtifactVersionRepository.findById(productId).orElse(
        new MavenArtifactVersion(productId)).getProductArtifactWithVersionReleased();
    List<String> versionsToDisplay = VersionUtils.getVersionsToDisplay(new ArrayList<>(cache.keySet()),
        isShowDevVersion, designerVersion);

    List<MavenArtifact> artifactsFromMeta = getArtifactsFromMeta(productId).stream()
        .filter(artifact -> !artifact.getArtifactId().endsWith(MavenConstants.PRODUCT_ARTIFACT_POSTFIX)).toList();
    Map<String, List<ArchivedArtifact>> archivedArtifactsMap = getArchivedArtifactMapFromMeta(artifactsFromMeta);
    List<MavenArtifactVersionModel> results = new ArrayList<>();

    for (String version : versionsToDisplay) {
      List<MavenArtifactModel> artifactsInVersion = convertArtifactsToModels(artifactsFromMeta, version,
          archivedArtifactsMap);
      List<MavenArtifactModel> productArtifactModels = cache.get(version);
      artifactsInVersion.addAll(productArtifactModels);
      results.add(new MavenArtifactVersionModel(version, artifactsInVersion.stream().distinct().toList()));
    }
    return results;
  }

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
      return GitHubUtils.extractMavenArtifactsFromContentStream(contentStream);
    } catch (IOException e) {
      log.error("Can not get maven artifacts from Product.json of {} - version {}:{}", productId, version,
          e.getMessage());
      return new ArrayList<>();
    }
  }

  public MavenArtifactModel convertMavenArtifactToModel(MavenArtifact artifact, String version,
      List<ArchivedArtifact> archivedArtifacts) {
    String artifactName = artifact.getName();
    if (StringUtils.isBlank(artifactName)) {
      artifactName = GitHubUtils.convertArtifactIdToName(artifact.getArtifactId());
    }
    artifact.setType(StringUtils.defaultIfBlank(artifact.getType(), ProductJsonConstants.DEFAULT_PRODUCT_TYPE));
    artifactName = String.format(MavenConstants.ARTIFACT_NAME_FORMAT, artifactName, artifact.getType());
    return new MavenArtifactModel(artifactName,
        buildDownloadUrlFromArtifactAndVersion(artifact, version, archivedArtifacts), artifact.getIsProductArtifact()
        , artifact.getArtifactId());
  }

  public List<MavenArtifactModel> convertArtifactsToModels(List<MavenArtifact> artifacts, String version,
      Map<String, List<ArchivedArtifact>> archivedArtifactsMap) {
    List<MavenArtifactModel> results = new ArrayList<>();
    if (!CollectionUtils.isEmpty(artifacts)) {
      for (MavenArtifact artifact : artifacts) {
        MavenArtifactModel mavenArtifactModel = convertMavenArtifactToModel(artifact, version,
            archivedArtifactsMap.get(artifact.getArtifactId()));
        results.add(mavenArtifactModel);
      }
    }
    return results;
  }

  public String buildDownloadUrlFromArtifactAndVersion(MavenArtifact artifact, String version,
      List<ArchivedArtifact> archivedArtifacts) {
    String groupIdByVersion = artifact.getGroupId();
    String artifactIdByVersion = artifact.getArtifactId();
    String repoUrl = StringUtils.defaultIfBlank(artifact.getRepoUrl(), MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL);
    ArchivedArtifact archivedArtifactBestMatchVersion = findArchivedArtifactInfoBestMatchWithVersion(version,
        archivedArtifacts);

    if (Objects.nonNull(archivedArtifactBestMatchVersion)) {
      groupIdByVersion = archivedArtifactBestMatchVersion.getGroupId();
      artifactIdByVersion = archivedArtifactBestMatchVersion.getArtifactId();
    }
    groupIdByVersion = groupIdByVersion.replace(CommonConstants.DOT_SEPARATOR, CommonConstants.SLASH);
    String artifactFileName = String.format(MavenConstants.ARTIFACT_FILE_NAME_FORMAT, artifactIdByVersion, version,
        artifact.getType());
    return String.join(CommonConstants.SLASH, repoUrl, groupIdByVersion, artifactIdByVersion, version,
        artifactFileName);
  }

  public ArchivedArtifact findArchivedArtifactInfoBestMatchWithVersion(String version,
      List<ArchivedArtifact> archivedArtifacts) {
    if (CollectionUtils.isEmpty(archivedArtifacts)) {
      return null;
    }
    return archivedArtifacts.stream()
        .filter(archivedArtifact -> MavenVersionComparator.compare(archivedArtifact.getLastVersion(), version) >= 0)
        .findAny().orElse(null);
  }
}
