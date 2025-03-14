package com.axonivy.market.service.impl;

import com.axonivy.market.entity.Artifact;
import com.axonivy.market.entity.MavenArtifactModel;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.repository.MavenArtifactModelRepository;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.MetadataRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.service.MetadataService;
import com.axonivy.market.util.MavenUtils;
import com.axonivy.market.util.MetadataReaderUtils;
import com.axonivy.market.util.VersionUtils;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Service
@AllArgsConstructor
@Log4j2
public class MetadataServiceImpl implements MetadataService {
  private final ProductJsonContentRepository productJsonRepo;
  private final MetadataRepository metadataRepo;
  private final MavenArtifactModelRepository mavenArtifactModelRepo;


  public void updateMavenArtifactVersionWithModel(List<MavenArtifactModel> artifactModelsInVersions,
      String version, Metadata metadata) {
    MavenArtifactModel model = MavenUtils.buildMavenArtifactModelFromMetadata(version, metadata);
    int existingModelIndex = findArtifactIndex(artifactModelsInVersions, metadata.getArtifactId(), version,
        !metadata.isProductArtifact());

    if (existingModelIndex != -1) {
      model.setId(artifactModelsInVersions.get(existingModelIndex).getId());
      artifactModelsInVersions.set(existingModelIndex, model);
    } else {
      artifactModelsInVersions.add(model);
    }
  }

  private int findArtifactIndex(List<MavenArtifactModel> artifactModels, String artifactId, String productVersion,
      boolean isProductArtifact) {
    return IntStream.range(0, artifactModels.size())
        .filter(i -> artifactModels.get(i).getArtifactId().equals(artifactId) &&
            artifactModels.get(i).getProductVersion().equals(productVersion) &&
            artifactModels.get(i).isAdditionalVersion() == isProductArtifact)
        .findFirst()
        .orElse(-1);
  }

  public void updateMavenArtifactVersionData(Set<Metadata> metadataSet, String productId) {
    List<MavenArtifactModel> artifactModelsInVersions = mavenArtifactModelRepo.findByProductId(productId);

    for (Metadata metadata : metadataSet) {
      String metadataContent = MavenUtils.getMetadataContentFromUrl(metadata.getUrl());
      if (StringUtils.isBlank(metadataContent)) {
        continue;
      }
      Metadata metadataWithVersions = MetadataReaderUtils.updateMetadataFromMavenXML(metadataContent, metadata, false);
      updateMavenArtifactVersionFromMetadata(artifactModelsInVersions, metadataWithVersions);
    }
    mavenArtifactModelRepo.saveAll(artifactModelsInVersions);
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

  public void updateMavenArtifactVersionFromMetadata(List<MavenArtifactModel> artifactModelsInVersions,
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

  public void updateMavenArtifactVersionForNonReleaseDevVersion(List<MavenArtifactModel> artifactModelsInVersions,
      Metadata metadata, String version) {
    Metadata snapShotMetadata = MavenUtils.buildSnapShotMetadataFromVersion(metadata, version);
    String xmlDataForSnapshotMetadata = MavenUtils.getMetadataContentFromUrl(snapShotMetadata.getUrl());
    MetadataReaderUtils.updateMetadataFromMavenXML(xmlDataForSnapshotMetadata, snapShotMetadata, true);
    updateMavenArtifactVersionWithModel(artifactModelsInVersions, version, snapShotMetadata);
  }
}
