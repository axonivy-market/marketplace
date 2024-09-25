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
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Log4j2
public class MetadataServiceImpl implements MetadataService {
  private final ProductRepository productRepo;
  private final MetadataSyncRepository metadataSyncRepo;
  private final ProductJsonContentRepository productJsonRepo;
  private final MavenArtifactVersionRepository mavenArtifactVersionRepo;
  private final MetadataRepository metadataRepo;


  public MetadataServiceImpl(ProductRepository productRepo, MetadataSyncRepository metadataSyncRepo,
      ProductJsonContentRepository productJsonRepo, MavenArtifactVersionRepository mavenArtifactVersionRepo,
      MetadataRepository metadataRepo) {
    this.productRepo = productRepo;
    this.metadataSyncRepo = metadataSyncRepo;
    this.productJsonRepo = productJsonRepo;
    this.mavenArtifactVersionRepo = mavenArtifactVersionRepo;
    this.metadataRepo = metadataRepo;
  }

  private static void updateMavenArtifactVersionForStableVersion(MavenArtifactVersion artifactVersionCache,
      Metadata metadata, String version) {
    boolean isExist = artifactVersionCache.getProductArtifactsByVersion().computeIfAbsent(version,
        k -> new ArrayList<>()).stream().anyMatch(
        artifact -> StringUtils.equals(metadata.getName(), artifact.getName()));
    if (!isExist) {
      MavenArtifactModel model = MavenArtifactModel.builder().name(metadata.getName()).downloadUrl(
          MavenUtils.buildDownloadUrl(metadata, version)).isInvalidArtifact(
          !metadata.getArtifactId().contains(metadata.getProductId())).build();
      artifactVersionCache.getProductArtifactsByVersion().get(version).add(model);
    }
  }

  private void updateMavenArtifactVersionData(Set<Metadata> metadataSet, MavenArtifactVersion artifactVersionCache) {
    for (Metadata metadata : metadataSet) {
      String metadataContent = MavenUtils.getMetadataContentFromUrl(metadata.getUrl());
      if (StringUtils.isBlank(metadataContent)) {
        continue;
      }
      MetadataReaderUtils.parseMetadataFromString(metadataContent, metadata);
      updateMavenArtifactVersionFromMetadata(artifactVersionCache, metadata);
    }
  }

  @Override
  public void syncAllProductMavenMetadata() {
    List<Product> products = productRepo.getAllProductWithIdAndReleaseTag();
    log.warn("**MetadataService: Start to sync version for {} product(s)", products.size());
    for (Product product : products) {
      // Set up cache before sync
      String productId = product.getId();
      Set<Metadata> metadataSet = new HashSet<>(metadataRepo.findByProductId(product.getId()));
      MavenArtifactVersion artifactVersionCache = mavenArtifactVersionRepo.findById(product.getId()).orElse(
          new MavenArtifactVersion(productId, new HashMap<>()));
      MetadataSync syncCache = metadataSyncRepo.findById(product.getId()).orElse(
          MetadataSync.builder().productId(product.getId()).syncedTags(new HashSet<>()).build());
      Set<Artifact> artifactsFromNewTags = new HashSet<>();

      // Find artifacts form unhandled tags
      List<String> nonSyncedVersionOfTags = getNonSyncedVersionOfTags(product.getReleasedVersions(), syncCache);
      if (!CollectionUtils.isEmpty(nonSyncedVersionOfTags)) {
        updateArtifactsFromNonSyncedVersion(product.getId(), nonSyncedVersionOfTags, artifactsFromNewTags);
        log.info("**MetadataService: New tags detected: {} in product {}", nonSyncedVersionOfTags.toString(),
            productId);
      }

      // Sync versions from maven & update artifacts-version table
      metadataSet.addAll(MavenUtils.convertArtifactsToMetadataSet(artifactsFromNewTags, productId));
      if (CollectionUtils.isEmpty(metadataSet)) {
        continue;
      }
      updateMavenArtifactVersionData(metadataSet, artifactVersionCache);

      // Persist changed
      syncCache.getSyncedTags().addAll(nonSyncedVersionOfTags);
      metadataSyncRepo.save(syncCache);
      mavenArtifactVersionRepo.save(artifactVersionCache);
      metadataRepo.saveAll(metadataSet);
    }
    log.warn("**MetadataService: version sync finished");
  }

  private void updateMavenArtifactVersionFromMetadata(MavenArtifactVersion artifactVersionCache, Metadata metadata) {
    metadata.getVersions().forEach(version -> {
      if (VersionUtils.isSnapshotVersion(version)) {
        if (VersionUtils.isOfficialVersionOrUnReleasedDevVersion(metadata.getVersions().stream().toList(), version)) {
          updateMavenArtifactVersionForNonReleaseDeVersion(artifactVersionCache, metadata, version);
        }
      } else {
        updateMavenArtifactVersionForStableVersion(artifactVersionCache, metadata, version);
      }
    });
  }

  private void updateMavenArtifactVersionForNonReleaseDeVersion(MavenArtifactVersion artifactVersionCache,
      Metadata metadata, String version) {
    Metadata snapShotMetadata = MavenUtils.buildSnapShotMetadataFromVersion(metadata, version);
    MetadataReaderUtils.parseMetadataSnapshotFromString(MavenUtils.getMetadataContentFromUrl(snapShotMetadata.getUrl()),
        snapShotMetadata);
    artifactVersionCache.getProductArtifactsByVersion().computeIfAbsent(version, k -> new ArrayList<>()).add(
        MavenUtils.buildMavenArtifactModelFromSnapShotMetadata(version, snapShotMetadata));
  }

  private void updateArtifactsFromNonSyncedVersion(String productId, List<String> nonSyncedVersions,
      Set<Artifact> artifacts) {
    if (CollectionUtils.isEmpty(nonSyncedVersions)) {
      return;
    }
    nonSyncedVersions.forEach(version -> {
      ProductJsonContent productJson = productJsonRepo.findByProductIdAndVersion(productId, version);
      List<Artifact> artifactsInVersion = MavenUtils.getMavenArtifactsFromProductJson(productJson);
      artifacts.addAll(artifactsInVersion);
    });
  }

  private List<String> getNonSyncedVersionOfTags(List<String> releasedVersion, MetadataSync cache) {
    if (!CollectionUtils.isEmpty(cache.getSyncedTags())) {
      releasedVersion.removeAll(cache.getSyncedTags());
    }
    return releasedVersion;
  }
}