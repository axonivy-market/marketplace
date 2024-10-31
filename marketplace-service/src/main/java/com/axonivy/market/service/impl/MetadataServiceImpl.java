package com.axonivy.market.service.impl;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.entity.MetadataSync;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.model.MavenArtifactModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.MetadataRepository;
import com.axonivy.market.repository.MetadataSyncRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductRepository;
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
import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
@Log4j2
public class MetadataServiceImpl implements MetadataService {
  private final ProductRepository productRepo;
  private final MetadataSyncRepository metadataSyncRepo;
  private final ProductJsonContentRepository productJsonRepo;
  private final MavenArtifactVersionRepository mavenArtifactVersionRepo;
  private final MetadataRepository metadataRepo;


  public void updateMavenArtifactVersionCacheWithModel(MavenArtifactVersion artifactVersionCache,
      String version, Metadata metadata) {
    List<MavenArtifactModel> artifactModelsInVersion =
        artifactVersionCache.getProductArtifactsByVersion().computeIfAbsent(
            version, k -> new ArrayList<>());
    if (metadata.isProductArtifact()) {
      if (artifactModelsInVersion.stream().anyMatch(artifact -> StringUtils.equals(metadata.getName(),
          artifact.getName()))) {
        return;
      }
      artifactModelsInVersion.add(
          MavenUtils.buildMavenArtifactModelFromMetadata(version, metadata));
    } else {
      artifactVersionCache.getAdditionalArtifactsByVersion().computeIfAbsent(version, k -> new ArrayList<>()).add(
          MavenUtils.buildMavenArtifactModelFromMetadata(version, metadata));
    }
  }

  public void updateMavenArtifactVersionData(Set<Metadata> metadataSet,
      MavenArtifactVersion artifactVersionCache) {
    for (Metadata metadata : metadataSet) {
      String metadataContent = MavenUtils.getMetadataContentFromUrl(metadata.getUrl());
      if (StringUtils.isBlank(metadataContent)) {
        continue;
      }
      Metadata metadataWithVersions = MetadataReaderUtils.updateMetadataFromMavenXML(metadataContent, metadata, false);
      updateMavenArtifactVersionFromMetadata(artifactVersionCache, metadataWithVersions);
    }
  }

  public int syncAllProductsMetadata() {
    List<Product> products = productRepo.getAllProductsWithIdAndReleaseTagAndArtifact();
    log.warn("**MetadataService: Start to sync version for {} product(s)", products.size());
    int nonUpdatedSyncCount = 0;
    for (Product product : products) {
      if (!syncProductMetadata(product)) {
        nonUpdatedSyncCount += 1;
      }
    }
    log.warn("**MetadataService: version sync finished");
    return nonUpdatedSyncCount;
  }

  @Override
  public boolean syncProductMetadata(Product product) {
    if (product == null) {
      return false;
    }

    // Set up cache before sync
    String productId = product.getId();
    Set<Metadata> metadataSet = new HashSet<>(metadataRepo.findByProductId(product.getId()));
    MavenArtifactVersion artifactVersionCache = mavenArtifactVersionRepo.findById(product.getId()).orElse(
        MavenArtifactVersion.builder().productId(productId).build());
    MetadataSync syncCache = metadataSyncRepo.findById(product.getId()).orElse(
        MetadataSync.builder().productId(product.getId()).syncedVersions(new HashSet<>()).build());
    Set<Artifact> artifactsFromNewTags = new HashSet<>();

    // Find artifacts from unhandled tags
    List<String> nonSyncedVersionOfTags = VersionUtils.removeSyncedVersionsFromReleasedVersions(
        product.getReleasedVersions(), syncCache.getSyncedVersions());
    if (ObjectUtils.isNotEmpty(nonSyncedVersionOfTags)) {
      artifactsFromNewTags.addAll(getArtifactsFromNonSyncedVersion(product.getId(), nonSyncedVersionOfTags));
      syncCache.getSyncedVersions().addAll(nonSyncedVersionOfTags);
      log.info("**MetadataService: New tags detected: {} in product {}", nonSyncedVersionOfTags.toString(),
          productId);
    }

    // Sync versions from maven & update artifacts-version table
    metadataSet.addAll(MavenUtils.convertArtifactsToMetadataSet(artifactsFromNewTags, productId));
    if (ObjectUtils.isNotEmpty(product.getArtifacts())) {
      metadataSet.addAll(
          MavenUtils.convertArtifactsToMetadataSet(new HashSet<>(product.getArtifacts()), productId));
    }
    if (CollectionUtils.isEmpty(metadataSet)) {
      log.info("**MetadataService: No artifact found in product {}", productId);
      return false;
    }
    artifactVersionCache.setAdditionalArtifactsByVersion(new HashMap<>());
    updateMavenArtifactVersionData(metadataSet, artifactVersionCache);

    // Persist changed
    metadataSyncRepo.save(syncCache);
    mavenArtifactVersionRepo.save(artifactVersionCache);
    metadataRepo.saveAll(metadataSet);
    return true;
  }

  @Override
  public void updateArtifactAndMetaDataForProductJsonContent(ProductJsonContent productJsonContent, Artifact productArtifact) {
    if (ObjectUtils.isEmpty(productJsonContent)) {
      return;
    }

    List<Artifact> artifactsInVersion = MavenUtils.getMavenArtifactsFromProductJson(productJsonContent);
    Optional.ofNullable(productArtifact).ifPresent(artifactsInVersion::add);
    log.info("**MetadataService: There are {} artifact(s) found in product {}",
        artifactsInVersion.size(), productJsonContent.getProductId());
    updateArtifactAndMetadata(productJsonContent.getProductId(), artifactsInVersion);
  }

  @Override
  public void updateArtifactAndMetadata(String productId, List<Artifact> artifacts) {
    Set<Metadata> metadataSet = new HashSet<>();
    for (Artifact artifact : artifacts) {
      String metadataUrl = MavenUtils.buildMetadataUrlFromArtifactInfo(artifact.getRepoUrl(), artifact.getGroupId(),
          artifact.getArtifactId());
      metadataSet.add(MavenUtils.convertArtifactToMetadata(productId, artifact, metadataUrl));
      metadataSet.addAll(MavenUtils.extractMetaDataFromArchivedArtifacts(productId, artifact));
    }

    if (CollectionUtils.isEmpty(metadataSet)) {
      log.info("**MetadataService: No artifact found in product {}", productId);
      return;
    }

    MavenArtifactVersion artifactVersionVersion = mavenArtifactVersionRepo.findById(productId)
        .orElse(MavenArtifactVersion.builder().productId(productId).build());

    artifactVersionVersion.setAdditionalArtifactsByVersion(new HashMap<>());
    updateMavenArtifactVersionData(metadataSet, artifactVersionVersion);

    mavenArtifactVersionRepo.save(artifactVersionVersion);
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
    MetadataReaderUtils.updateMetadataFromMavenXML(MavenUtils.getMetadataContentFromUrl(snapShotMetadata.getUrl()),
        snapShotMetadata, true);
    updateMavenArtifactVersionCacheWithModel(artifactVersionCache, version, snapShotMetadata);
  }

  public Set<Artifact> getArtifactsFromNonSyncedVersion(String productId, List<String> nonSyncedVersions) {
    Set<Artifact> artifacts = new HashSet<>();
    if (CollectionUtils.isEmpty(nonSyncedVersions)) {
      return artifacts;
    }
    nonSyncedVersions.forEach(version -> {
      ProductJsonContent productJson =
          productJsonRepo.findByProductIdAndVersion(productId, version).stream().findAny().orElse(null);
      List<Artifact> artifactsInVersion = MavenUtils.getMavenArtifactsFromProductJson(productJson);
      artifacts.addAll(artifactsInVersion);
    });
    return artifacts;
  }
}
