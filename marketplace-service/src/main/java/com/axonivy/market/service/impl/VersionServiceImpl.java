package com.axonivy.market.service.impl;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.comparator.LatestVersionComparator;
import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.factory.VersionFactory;
import com.axonivy.market.model.MavenArtifactModel;
import com.axonivy.market.model.MavenArtifactVersionModel;
import com.axonivy.market.model.VersionAndUrlModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.MetadataRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductModuleContentRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.VersionService;
import com.axonivy.market.util.MavenUtils;
import com.axonivy.market.util.VersionUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.axonivy.market.constants.ProductJsonConstants.NAME;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Log4j2
@Service
@AllArgsConstructor
public class VersionServiceImpl implements VersionService {

  private final MavenArtifactVersionRepository mavenArtifactVersionRepository;
  private final ProductRepository productRepo;
  private final ProductJsonContentRepository productJsonRepo;
  private final ProductModuleContentRepository productContentRepo;
  private final ObjectMapper mapper = new ObjectMapper();
  private final MetadataRepository metadataRepo;

  public List<MavenArtifactVersionModel> getArtifactsAndVersionToDisplay(String productId, Boolean isShowDevVersion,
      String designerVersion) {
    MavenArtifactVersion existingMavenArtifactVersion = mavenArtifactVersionRepository.findById(productId).orElse(
        MavenArtifactVersion.builder().productId(productId).build());
    List<MavenArtifactVersionModel> results = new ArrayList<>();

    for (String mavenVersion : MavenUtils.getAllExistingVersions(existingMavenArtifactVersion, isShowDevVersion,
        designerVersion)) {
      List<MavenArtifactModel> artifactsByVersion = new ArrayList<>();
      artifactsByVersion.addAll(
          existingMavenArtifactVersion.getProductArtifactsByVersion().computeIfAbsent(mavenVersion,
              k -> new ArrayList<>()));
      artifactsByVersion.addAll(
          existingMavenArtifactVersion.getAdditionalArtifactsByVersion().computeIfAbsent(mavenVersion,
              k -> new ArrayList<>()));

      if (ObjectUtils.isNotEmpty(artifactsByVersion)) {
        artifactsByVersion = artifactsByVersion.stream().distinct().toList();
        results.add(new MavenArtifactVersionModel(mavenVersion, artifactsByVersion));
      }
    }
    return results;
  }

  public Map<String, Object> getProductJsonContentByIdAndTag(String productId, String tag) {
    Map<String, Object> result = new HashMap<>();
    try {
      ProductJsonContent productJsonContent =
          productJsonRepo.findByProductIdAndVersion(productId, tag).stream().findAny().orElse(null);
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
    MavenArtifactVersion existingMavenArtifactVersion = mavenArtifactVersionRepository.findById(productId).orElse(
        MavenArtifactVersion.builder().productId(productId).build());
    List<String> versions = MavenUtils.getAllExistingVersions(existingMavenArtifactVersion, true,
        null);
    for (String version : versions) {
      Link link = linkTo(
          methodOn(ProductDetailsController.class).findProductJsonContent(productId, version)).withSelfRel();
      VersionAndUrlModel versionAndUrlModel = new VersionAndUrlModel(version, link.getHref());
      versionAndUrlList.add(versionAndUrlModel);
    }
    return versionAndUrlList;
  }

  public List<String> getPersistedVersions(String productId) {
    var product = productRepo.findById(productId);
    List<String> versions = new ArrayList<>();
    if (product.isPresent()) {
      versions.addAll(product.get().getReleasedVersions());
    }
    if (CollectionUtils.isEmpty(versions)) {
      versions.addAll(productContentRepo.findTagsByProductId(productId));
      versions = versions.stream().map(VersionUtils::convertTagToVersion).collect(Collectors.toList());
    }
    return versions;
  }

  public List<Artifact> getMavenArtifactsFromProductJsonByTag(String tag,
      String productId) {
    ProductJsonContent productJson =
        productJsonRepo.findByProductIdAndVersion(productId, tag).stream().findAny().orElse(null);
    return MavenUtils.getMavenArtifactsFromProductJson(productJson);
  }

  public String getLatestVersionArtifactDownloadUrl(String productId, String version, String artifactId) {
    List<Metadata> artifactsMetadata = metadataRepo.findByProductIdAndArtifactId(productId, artifactId);
    if (CollectionUtils.isEmpty(artifactsMetadata)) {
      return StringUtils.EMPTY;
    }
    String artifactName = artifactsMetadata.get(0).getName();

    String targetVersion = VersionFactory.getFromMetadata(artifactsMetadata, version);
    if (StringUtils.isBlank(targetVersion)) {
      return StringUtils.EMPTY;
    }
    var artifactVersionCache = mavenArtifactVersionRepository.findById(productId);
    if (artifactVersionCache.isEmpty()) {
      return StringUtils.EMPTY;
    }

    String downloadUrl = artifactVersionCache.get().getProductArtifactsByVersion().computeIfAbsent(targetVersion,
        key -> new ArrayList<>()).stream().filter(
        artifact -> StringUtils.equals(artifactName, artifact.getName())).findAny().map(
        MavenArtifactModel::getDownloadUrl).orElse(null);
    if (StringUtils.isBlank(downloadUrl)) {
      downloadUrl = artifactVersionCache.get().getAdditionalArtifactsByVersion().computeIfAbsent(targetVersion,
          key -> new ArrayList<>()).stream().filter(
          artifact -> StringUtils.equals(artifactName, artifact.getName())).findAny().map(
          MavenArtifactModel::getDownloadUrl).orElse(null);
    }
    return downloadUrl;
  }
}
