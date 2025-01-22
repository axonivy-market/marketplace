package com.axonivy.market.service.impl;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.model.MavenArtifactModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.MetadataRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.service.MetadataService;
import com.axonivy.market.util.MavenUtils;
import com.axonivy.market.util.MetadataReaderUtils;
import com.axonivy.market.util.VersionUtils;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
@Log4j2
public class MetadataServiceImpl implements MetadataService {
  private final ProductJsonContentRepository productJsonRepo;
  private final MavenArtifactVersionRepository mavenArtifactVersionRepo;
  private final MetadataRepository metadataRepo;

  public void updateMavenArtifactVersionCacheWithModel(MavenArtifactVersion artifactVersionCache,
      String version, Metadata metadata) {
    List<MavenArtifactModel> artifactModelsInVersion;
    if (metadata.isProductArtifact()) {
      artifactModelsInVersion =
          artifactVersionCache.getProductArtifactsByVersion().computeIfAbsent(version, k -> new ArrayList<>());
    } else {
      artifactModelsInVersion = artifactVersionCache.getAdditionalArtifactsByVersion().computeIfAbsent(version,
          k -> new ArrayList<>());
    }
    updateMavenArtifactVersionModel(artifactModelsInVersion, version, metadata);
  }

  public void updateMavenArtifactVersionModel(List<MavenArtifactModel> artifactModelsInVersions, String version,
      Metadata metadata) {
    // Always update the download url for snapshot artifacts
    if (!VersionUtils.isMajorVersion(version)) {
      artifactModelsInVersions.removeIf(existingModel -> StringUtils.equals(existingModel.getArtifactId(),
          metadata.getArtifactId()));
    }
    MavenArtifactModel model = MavenUtils.buildMavenArtifactModelFromMetadata(version, metadata);
    artifactModelsInVersions.add(model);
  }

  public void updateMavenArtifactVersionData(Set<Metadata> metadataSet, MavenArtifactVersion artifactVersionCache) {
    for (Metadata metadata : metadataSet) {
      String metadataContent = MavenUtils.getMetadataContentFromUrl(metadata.getUrl());
      if (StringUtils.isBlank(metadataContent)) {
        continue;
      }
      Metadata metadataWithVersions = MetadataReaderUtils.updateMetadataFromMavenXML(metadataContent, metadata, false);
      updateMavenArtifactVersionFromMetadata(artifactVersionCache, metadataWithVersions);
    }
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

    MavenArtifactVersion artifactVersion = mavenArtifactVersionRepo.findById(productId)
        .orElse(MavenArtifactVersion.builder().productId(productId).build());

    artifactVersion.setAdditionalArtifactsByVersion(new HashMap<>());
    updateMavenArtifactVersionData(metadataSet, artifactVersion);

    mavenArtifactVersionRepo.save(artifactVersion);
    metadataRepo.saveAll(metadataSet);
  }

  public void updateMavenArtifactVersionFromMetadata(MavenArtifactVersion artifactVersionCache,
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
        updateMavenArtifactVersionForNonReleaseDevVersion(artifactVersionCache, metadata, version);
      } else if (!isSnapshotVersion) {
        updateMavenArtifactVersionCacheWithModel(artifactVersionCache, version, metadata);
      }
    }
  }

  public void updateMavenArtifactVersionForNonReleaseDevVersion(MavenArtifactVersion artifactVersionCache,
      Metadata metadata, String version) {
    Metadata snapShotMetadata = MavenUtils.buildSnapShotMetadataFromVersion(metadata, version);
    String xmlDataForSnapshotMetadata = MavenUtils.getMetadataContentFromUrl(snapShotMetadata.getUrl());
    MetadataReaderUtils.updateMetadataFromMavenXML(xmlDataForSnapshotMetadata, snapShotMetadata, true);
    updateMavenArtifactVersionCacheWithModel(artifactVersionCache, version, snapShotMetadata);
  }

  public Set<Artifact> getArtifactsFromNonSyncedVersion(String productId, List<String> nonSyncedVersions) {
    Set<Artifact> artifacts = new HashSet<>();
    if (CollectionUtils.isEmpty(nonSyncedVersions)) {
      return artifacts;
    }

    List<ProductJsonContent> productJsonContents = productJsonRepo.findByProductIdAndVersionIn(productId, nonSyncedVersions);
    for (ProductJsonContent productJsonContent : productJsonContents) {
      List<Artifact> artifactsInVersion = MavenUtils.getMavenArtifactsFromProductJson(productJsonContent);
      artifacts.addAll(artifactsInVersion);
    }

    return artifacts;
  }
}
