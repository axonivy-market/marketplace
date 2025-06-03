package com.axonivy.market.service.impl;

import com.axonivy.market.comparator.LatestVersionComparator;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.factory.VersionFactory;
import com.axonivy.market.model.MavenArtifactVersionModel;
import com.axonivy.market.model.VersionAndUrlModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.MetadataRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.service.ProductMarketplaceDataService;
import com.axonivy.market.service.VersionService;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.axonivy.market.constants.MavenConstants.TEST_ARTIFACTID;
import static com.axonivy.market.constants.ProductJsonConstants.NAME;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Log4j2
@Service
@AllArgsConstructor
public class VersionServiceImpl implements VersionService {

  private final ProductJsonContentRepository productJsonRepo;
  private final ProductMarketplaceDataService productMarketplaceDataService;
  private final ObjectMapper mapper = new ObjectMapper();
  private final MetadataRepository metadataRepo;
  private final MavenArtifactVersionRepository mavenArtifactVersionRepo;

  public List<MavenArtifactVersionModel> getArtifactsAndVersionToDisplay(String productId, Boolean isShowDevVersion,
      String designerVersion) {
    List<MavenArtifactVersion> mavenArtifactVersions = mavenArtifactVersionRepo.findByProductId(productId);

    List<String> mavenVersions = VersionUtils.extractAllVersions(mavenArtifactVersions, isShowDevVersion, designerVersion);

    List<MavenArtifactVersionModel> results = new ArrayList<>();
    for (String mavenVersion : mavenVersions) {
      List<MavenArtifactVersion> artifactsByVersion = filterArtifactByVersion(mavenArtifactVersions, mavenVersion);

      if (ObjectUtils.isNotEmpty(artifactsByVersion)) {
        results.add(new MavenArtifactVersionModel(mavenVersion, artifactsByVersion));
      }
    }
    return results;
  }

  private List<MavenArtifactVersion> filterArtifactByVersion(List<MavenArtifactVersion> mavenArtifactVersions,
      String mavenVersion) {
    return mavenArtifactVersions.stream()
        .filter(artifact -> artifact.getId().getProductVersion().equals(mavenVersion))
        .distinct()
        .sorted(
            Comparator.comparing((MavenArtifactVersion artifact) -> artifact.getId().getArtifactId())
                .thenComparing(artifact -> artifact.getId().getArtifactId().endsWith(TEST_ARTIFACTID)))
        .toList();
  }

  public Map<String, Object> getProductJsonContentByIdAndVersion(String productId, String version,
      String designerVersion) {
    Map<String, Object> result = new HashMap<>();
    try {
      ProductJsonContent productJsonContent =
          productJsonRepo.findByProductIdAndVersion(productId, version).stream().findAny().orElse(null);
      if (ObjectUtils.isEmpty(productJsonContent)) {
        return new HashMap<>();
      }
      result = mapper.readValue(productJsonContent.getContent(), Map.class);
      result.computeIfAbsent(NAME, k -> productJsonContent.getName());
      productMarketplaceDataService.updateInstallationCountForProduct(productId, designerVersion);
    } catch (JsonProcessingException jsonProcessingException) {
      log.error(jsonProcessingException.getMessage());
    }
    return result;
  }

  @Override
  public List<VersionAndUrlModel> getVersionsForDesigner(String productId,
      Boolean isShowDevVersion, String designerVersion) {
    List<VersionAndUrlModel> versionAndUrlList = new ArrayList<>();
    List<String> releasedVersions =
        VersionUtils.getInstallableVersionsFromMetadataList(metadataRepo.findByProductId(productId));
    if (CollectionUtils.isEmpty(releasedVersions)) {
      return Collections.emptyList();
    }
    for (String version : VersionUtils.getVersionsToDisplay(releasedVersions, isShowDevVersion)) {
      Link link = linkTo(
          methodOn(ProductDetailsController.class).findProductJsonContent(productId, version,
              designerVersion)).withSelfRel();
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

    List<MavenArtifactVersion> artifactModels = mavenArtifactVersionRepo.findByProductId(productId);
    if (ObjectUtils.isEmpty(artifactModels)) {
      return StringUtils.EMPTY;
    }

    // Find download url first from product artifact model
    String downloadUrl = getDownloadUrlFromExistingDataByArtifactIdAndVersion(
        artifactModels, targetVersion, modelArtifactIds);


    if (!StringUtils.endsWith(downloadUrl, fileType)) {
      log.warn("**VersionService: the found downloadUrl {} is not match with file type {}", downloadUrl, fileType);
      downloadUrl = StringUtils.EMPTY;
    }
    return downloadUrl;
  }

  public String getDownloadUrlFromExistingDataByArtifactIdAndVersion(List<MavenArtifactVersion> existingData,
      String version, List<String> artifactsIds) {
    return existingData.stream()
        .filter(
            artifact -> version.equals(artifact.getId().getProductVersion()) &&
                artifactsIds.contains(artifact.getId().getArtifactId()))
        .min(Comparator.comparing(artifact -> artifact.getId().isAdditionalVersion()))
        .map(MavenArtifactVersion::getDownloadUrl)
        .orElse(null);
  }

}
