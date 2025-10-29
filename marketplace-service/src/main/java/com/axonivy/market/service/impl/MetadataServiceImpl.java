package com.axonivy.market.service.impl;

import com.axonivy.market.entity.Artifact;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.MetadataRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.service.MetadataService;
import com.axonivy.market.util.MavenUtils;
import com.axonivy.market.util.MetadataReaderUtils;
import com.axonivy.market.util.VersionUtils;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Dependency;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.axonivy.market.constants.MavenConstants.DEFAULT_IVY_MIRROR_MAVEN_BASE_URL;
import static com.axonivy.market.constants.MavenConstants.ARTIFACT_NAME_FORMAT;
import static com.axonivy.market.constants.ProductJsonConstants.DEFAULT_PRODUCT_TYPE;

@Service
@AllArgsConstructor
@Log4j2
public class MetadataServiceImpl implements MetadataService {
  private final ProductJsonContentRepository productJsonRepo;
  private final MetadataRepository metadataRepo;
  private final MavenArtifactVersionRepository mavenArtifactVersionRepo;
  private final FileDownloadService fileDownloadService;

  @Override
  public Metadata getMetadataByVersion(Dependency dependencyModel, String version) {
    List<Metadata> existingMetadata = metadataRepo.findByGroupIdAndArtifactId(
        dependencyModel.getGroupId(), dependencyModel.getArtifactId());

    Optional<Metadata> metadataWithVersion = findMetadataWithVersion(existingMetadata, version);
    if (metadataWithVersion.isPresent()) {
      return metadataWithVersion.get();
    }

    var metadata = existingMetadata.stream().findFirst()
        .orElseGet(() -> createNewMetadata(dependencyModel));

    return updateWithVersion(metadata, version);
  }

  public void updateMavenArtifactVersionWithModel(Collection<MavenArtifactVersion> artifactModelsInVersions,
      String version, Metadata metadata) {
    var model = MavenUtils.buildMavenArtifactVersionFromMetadata(version, metadata);
    artifactModelsInVersions.removeIf(artifactVersion -> artifactVersion.getId().equals(model.getId()));
    artifactModelsInVersions.add(model);
  }

  public void updateMavenArtifactVersionData(Iterable<Metadata> metadataSet, String productId) {
    List<MavenArtifactVersion> artifactModelsInVersions = mavenArtifactVersionRepo.findByProductId(productId);

    for (Metadata metadata : metadataSet) {
      String metadataContent = fileDownloadService.getFileAsString(metadata.getUrl());
      if (StringUtils.isBlank(metadataContent)) {
        continue;
      }
      var metadataWithVersions = MetadataReaderUtils.updateMetadataFromMavenXML(metadataContent, metadata, false);
      updateMavenArtifactVersionFromMetadata(artifactModelsInVersions, metadataWithVersions);
    }
    mavenArtifactVersionRepo.saveAll(artifactModelsInVersions);
  }

  @Override
  public void updateArtifactAndMetadata(String productId, List<String> versions, List<Artifact> artifacts) {
    Set<Metadata> metadataSet = new HashSet<>(metadataRepo.findByProductId(productId));
    Set<Artifact> artifactsFromNewVersions = new HashSet<>();

    if (ObjectUtils.isNotEmpty(versions)) {
      List<ProductJsonContent> productJsonContents = productJsonRepo.findByProductIdAndVersionIn(productId, versions);
      for (ProductJsonContent productJsonContent : productJsonContents) {
        List<Artifact> artifactsFromNonSyncedVersions = MavenUtils.getMavenArtifactsFromProductJson(productJsonContent);
        artifactsFromNewVersions.addAll(artifactsFromNonSyncedVersions);
      }
      log.info("**MetadataService: New versions detected: {} in product {}", versions, productId);
    }

    metadataSet.addAll(MavenUtils.convertArtifactsToMetadataSet(artifactsFromNewVersions, productId));
    if (ObjectUtils.isNotEmpty(artifacts)) {
      metadataSet.addAll(MavenUtils.convertArtifactsToMetadataSet(new HashSet<>(artifacts), productId));
    }

    if (CollectionUtils.isEmpty(metadataSet)) {
      log.info("**MetadataService: No artifact found in product {}", productId);
      return;
    }
    updateMavenArtifactVersionData(metadataSet, productId);
    metadataRepo.saveAll(metadataSet);
  }

  public void updateMavenArtifactVersionFromMetadata(List<MavenArtifactVersion> artifactModelsInVersions,
      Metadata metadata) {
    // Skip to add new model for product artifact
    if (MavenUtils.isProductArtifactId(metadata.getArtifactId())) {
      return;
    }

    for (String version : metadata.getVersions()) {
      boolean isSnapshotVersion = VersionUtils.isSnapshotVersion(version);
      boolean isOfficialVersionOrUnReleasedDevVersion =
          VersionUtils.isOfficialVersionOrUnReleasedDevVersion(metadata.getVersions(), version);

      if (isSnapshotVersion && isOfficialVersionOrUnReleasedDevVersion) {
        updateMavenArtifactVersionForNonReleaseDevVersion(artifactModelsInVersions, metadata, version);
      } else if (!isSnapshotVersion) {
        updateMavenArtifactVersionWithModel(artifactModelsInVersions, version, metadata);
      }
    }
  }

  public void updateMavenArtifactVersionForNonReleaseDevVersion(List<MavenArtifactVersion> artifactModelsInVersions,
      Metadata metadata, String version) {
    var snapShotMetadata = MavenUtils.buildSnapShotMetadataFromVersion(metadata, version);
    String xmlDataForSnapshotMetadata = fileDownloadService.getFileAsString(snapShotMetadata.getUrl());
    MetadataReaderUtils.updateMetadataFromMavenXML(xmlDataForSnapshotMetadata, snapShotMetadata, true);
    updateMavenArtifactVersionWithModel(artifactModelsInVersions, version, snapShotMetadata);
  }

  private Metadata createNewMetadata(Dependency dependencyModel) {
    String metadataUrl = MavenUtils.buildMetadataUrlFromArtifactInfo(
        DEFAULT_IVY_MIRROR_MAVEN_BASE_URL,
        dependencyModel.getGroupId(),
        dependencyModel.getArtifactId());

    String artifactName = MavenUtils.convertArtifactIdToName(dependencyModel.getArtifactId());
    String type = StringUtils.defaultIfBlank(dependencyModel.getType(), DEFAULT_PRODUCT_TYPE);

    Metadata metadata = Metadata.builder()
        .groupId(dependencyModel.getGroupId())
        .artifactId(dependencyModel.getArtifactId())
        .versions(new HashSet<>())
        .productId(dependencyModel.getArtifactId())
        .url(metadataUrl)
        .repoUrl(DEFAULT_IVY_MIRROR_MAVEN_BASE_URL)
        .type(type)
        .name(String.format(ARTIFACT_NAME_FORMAT, artifactName, type))
        .isProductArtifact(false)
        .build();

    return metadataRepo.save(metadata);
  }

  private Metadata updateWithVersion(Metadata metadata, String version) {
    try {
      Set<Metadata> metadataSet = Set.of(metadata);
      updateMavenArtifactVersionData(metadataSet, metadata.getProductId());
      metadataRepo.flush();

      List<Metadata> metadataLists = metadataRepo.findByGroupIdAndArtifactId(
          metadata.getGroupId(), metadata.getArtifactId());
      return findMetadataWithVersion(metadataLists, version).orElse(metadata);

    } catch (Exception e) {
      log.error("Failed to update metadata for {} - {}: {}",
          metadata.getGroupId(), metadata.getArtifactId(), e.getMessage());
      return metadata;
    }
  }

  private static Optional<Metadata> findMetadataWithVersion(List<Metadata> metadataLists, String version) {
    return metadataLists.stream()
        .filter(meta -> meta.getVersions() != null && meta.getVersions().contains(version))
        .findFirst();
  }
}
