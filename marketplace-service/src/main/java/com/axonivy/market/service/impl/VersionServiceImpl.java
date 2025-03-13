package com.axonivy.market.service.impl;

import com.axonivy.market.comparator.LatestVersionComparator;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.entity.MavenArtifactModel;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.factory.VersionFactory;
import com.axonivy.market.model.MavenArtifactVersionModel;
import com.axonivy.market.model.VersionAndUrlModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.MetadataRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.axonivy.market.constants.ProductJsonConstants.NAME;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Log4j2
@Service
@AllArgsConstructor
public class VersionServiceImpl implements VersionService {

  private final MavenArtifactVersionRepository mavenArtifactVersionRepo;
  private final ProductJsonContentRepository productJsonRepo;
  private final ObjectMapper mapper = new ObjectMapper();
  private final MetadataRepository metadataRepo;

  public List<MavenArtifactVersionModel> getArtifactsAndVersionToDisplay(String productId, Boolean isShowDevVersion,
      String designerVersion) {
    MavenArtifactVersion mavenArtifactVersion = mavenArtifactVersionRepo.findById(productId)
        .orElse(MavenArtifactVersion.builder().productId(productId).build());
    List<MavenArtifactVersionModel> results = new ArrayList<>();
    List<String> mavenVersions = VersionUtils.extractAllVersions(mavenArtifactVersion, isShowDevVersion, designerVersion);
    for (String mavenVersion : mavenVersions) {
      List<MavenArtifactModel> artifactsByVersion = new ArrayList<>();
      artifactsByVersion.addAll(
          filterArtifactByVersion(mavenArtifactVersion.getProductArtifactsByVersion(), mavenVersion));
      artifactsByVersion.addAll(
          filterArtifactByVersion(mavenArtifactVersion.getAdditionalArtifactsByVersion(), mavenVersion));

      if (ObjectUtils.isNotEmpty(artifactsByVersion)) {
        artifactsByVersion = artifactsByVersion.stream().distinct().toList();
        results.add(new MavenArtifactVersionModel(mavenVersion, artifactsByVersion));
      }
    }
    return results;
  }

  private List<MavenArtifactModel> filterArtifactByVersion(List<MavenArtifactModel> mavenArtifactModels,
      String mavenVersion) {
    return mavenArtifactModels.stream()
        .filter(artifact -> artifact.getProductVersion().equals(mavenVersion))
        .toList();
  }

  public Map<String, Object> getProductJsonContentByIdAndVersion(String productId, String version) {
    Map<String, Object> result = new HashMap<>();
    try {
      ProductJsonContent productJsonContent =
          productJsonRepo.findByProductIdAndVersion(productId, version).stream().findAny().orElse(null);
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
    List<String> releasedVersions =
        VersionUtils.getInstallableVersionsFromMetadataList(metadataRepo.findByProductId(productId));
    if (CollectionUtils.isEmpty(releasedVersions)) {
      return Collections.emptyList();
    }
    List<String> versions = releasedVersions.stream().filter(
        version -> VersionUtils.isOfficialVersionOrUnReleasedDevVersion(releasedVersions, version)).sorted(
        new LatestVersionComparator()).toList();
    for (String version : versions) {
      Link link = linkTo(
          methodOn(ProductDetailsController.class).findProductJsonContent(productId, version)).withSelfRel();
      VersionAndUrlModel versionAndUrlModel = new VersionAndUrlModel(version, link.getHref());
      versionAndUrlList.add(versionAndUrlModel);
    }
    return versionAndUrlList;
  }

  public String getLatestVersionArtifactDownloadUrl(String productId, String version, String artifact) {
    String[] artifactParts = StringUtils.defaultString(artifact).split(MavenConstants.MAIN_VERSION_REGEX);
    if (artifactParts.length < 1) {
      return StringUtils.EMPTY;
    }

    String artifactId = artifactParts[0];
    String fileType = artifactParts[artifactParts.length - 1];
    List<Metadata> metadataList = metadataRepo.findByProductIdAndArtifactId(productId, artifactId);
    if (CollectionUtils.isEmpty(metadataList)) {
      return StringUtils.EMPTY;
    }

    List<String> modelArtifactIds = metadataList.stream().map(Metadata::getArtifactId).toList();
    String targetVersion = VersionFactory.getFromMetadata(metadataList, version);
    if (StringUtils.isBlank(targetVersion)) {
      return StringUtils.EMPTY;
    }

    MavenArtifactVersion artifactVersionCache = mavenArtifactVersionRepo.findById(productId).orElse(null);
    if (ObjectUtils.isEmpty(artifactVersionCache)) {
      return StringUtils.EMPTY;
    }

    // Find download url first from product artifact model
    String downloadUrl = getDownloadUrlFromExistingDataByArtifactIdAndVersion(
        artifactVersionCache.getProductArtifactsByVersion(), targetVersion, modelArtifactIds);

    // Continue to find download url from artifact in meta.json if it is not existed in artifacts of product.json
    if (StringUtils.isBlank(downloadUrl)) {
      downloadUrl = getDownloadUrlFromExistingDataByArtifactIdAndVersion(
          artifactVersionCache.getAdditionalArtifactsByVersion(), targetVersion, modelArtifactIds);
    }

    if (!StringUtils.endsWith(downloadUrl, fileType)) {
      log.warn("**VersionService: the found downloadUrl {} is not match with file type {}", downloadUrl, fileType);
      downloadUrl = StringUtils.EMPTY;
    }
    return downloadUrl;
  }

  public String getDownloadUrlFromExistingDataByArtifactIdAndVersion(List<MavenArtifactModel> existingData,
      String version, List<String> artifactsIds) {
    return existingData.stream()
        .filter(
            artifact -> version.equals(artifact.getProductVersion()) && artifactsIds.contains(artifact.getArtifactId()))
        .findAny()
        .map(MavenArtifactModel::getDownloadUrl)
        .orElse(null);
  }

}
