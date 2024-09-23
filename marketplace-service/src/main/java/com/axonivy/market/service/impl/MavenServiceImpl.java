package com.axonivy.market.service.impl;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.MavenVersionSync;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.bo.Artifact;
import com.axonivy.market.bo.Metadata;
import com.axonivy.market.service.MavenService;
import com.axonivy.market.util.MavenUtils;
import com.axonivy.market.util.MetadataReaderUtils;
import com.axonivy.market.model.MavenArtifactModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.MavenVersionSyncRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class MavenServiceImpl implements MavenService {
  private final ProductRepository productRepo;
  private final MavenVersionSyncRepository mavenRepo;
  private final ProductJsonContentRepository productJsonRepo;
  private final MavenArtifactVersionRepository mavenArtifactVersionRepo;

  public MavenServiceImpl(ProductRepository productRepo, MavenVersionSyncRepository mavenRepo,
      ProductJsonContentRepository productJsonRepo, MavenArtifactVersionRepository mavenArtifactVersionRepo) {
    this.productRepo = productRepo;
    this.mavenRepo = mavenRepo;
    this.productJsonRepo = productJsonRepo;
    this.mavenArtifactVersionRepo = mavenArtifactVersionRepo;
  }

  private static void updateCache(String version, Set<Metadata> metadataSet, MavenArtifactVersion artifactVersion) {
    metadataSet.forEach(metadata -> {
      if (metadata.getVersions().contains(version)) {
        MavenArtifactModel model = MavenArtifactModel.builder().name(metadata.getName()).downloadUrl(
            MavenUtils.buildDownloadUrl(metadata, version)).build();
        artifactVersion.getProductArtifactWithVersionReleased().computeIfAbsent(version, k -> new ArrayList<>()).add(
            model);
      }
    });
  }

  @Override
  public void syncAllArtifactFromMaven() {
    List<Product> products = productRepo.getAllProductWithIdAndReleaseTag();
    for (Product product : products) {
      Set<Artifact> artifacts = new HashSet<>();
      MavenVersionSync cache = mavenRepo.findById(product.getId()).orElse(
          MavenVersionSync.builder().productId(product.getId()).syncedVersions(new ArrayList<>()).build());
      List<String> nonSyncedVersions = getNonSyncedVersions(product, cache);
      if (CollectionUtils.isEmpty(nonSyncedVersions)) {
        continue;
      }
      updateArtifactsFromNonSyncedVersion(product, nonSyncedVersions, artifacts);

      Set<Metadata> metadataSet = convertArtifactsToMetadataSet(artifacts);
      MavenArtifactVersion artifactVersionCache = updateMavenArtifactVersionData(product, metadataSet,
          nonSyncedVersions);
      cache.getSyncedVersions().addAll(nonSyncedVersions);
      mavenRepo.save(cache);
      mavenArtifactVersionRepo.save(artifactVersionCache);
    }
  }

  private MavenArtifactVersion updateMavenArtifactVersionData(Product product, Set<Metadata> metadataSet,
      List<String> nonSyncedVersions) {
    metadataSet.forEach(version -> MetadataReaderUtils.extractDataFromUrl(version.getMetadataUrl(), version));
    MavenArtifactVersion artifactVersionCache = mavenArtifactVersionRepo.findById(product.getId()).orElse(
        new MavenArtifactVersion(product.getId(), new HashMap<>()));
    nonSyncedVersions.forEach(version -> updateCache(version, metadataSet, artifactVersionCache));
    return artifactVersionCache;
  }

  private void updateArtifactsFromNonSyncedVersion(Product product, List<String> nonSyncedVersions,
      Set<Artifact> artifacts) {
    if (CollectionUtils.isEmpty(nonSyncedVersions)) {
      return;
    }
    nonSyncedVersions.forEach(version -> {
      ProductJsonContent productJson = productJsonRepo.findByProductIdAndVersion(product.getId(), version);
      List<Artifact> artifactsInVersion = MavenUtils.getMavenArtifactsFromProductJson(productJson);
      artifacts.addAll(artifactsInVersion);
    });
  }

  private List<String> getNonSyncedVersions(Product product, MavenVersionSync cache) {
    List<String> nonSyncedVersions = product.getReleasedVersions();
    if (!CollectionUtils.isEmpty(cache.getSyncedVersions())) {
      nonSyncedVersions.removeAll(cache.getSyncedVersions()); // filter non synced version
    }
    return nonSyncedVersions;
  }

  private Set<Metadata> convertArtifactsToMetadataSet(Set<Artifact> artifacts) {
    Set<Metadata> results = new HashSet<>();
    if (!CollectionUtils.isEmpty(artifacts)) {
      artifacts.forEach(artifact -> {
        String metadataUrl = buildMetadataUrlFromArtifactInfo(artifact.getRepoUrl(), artifact.getGroupId(),
            artifact.getArtifactId());
        results.add(convertArtifactToMetadata(artifact, metadataUrl));
        extractedMetaDataFromArchivedArtifacts(artifact, results);
      });
    }
    return results;
  }

  private void extractedMetaDataFromArchivedArtifacts(Artifact artifact, Set<Metadata> results) {
    if (!CollectionUtils.isEmpty(artifact.getArchivedArtifacts())) {
      artifact.getArchivedArtifacts().forEach(archivedArtifact -> {
        String archivedMetadataUrl = buildMetadataUrlFromArtifactInfo(artifact.getRepoUrl(),
            archivedArtifact.getGroupId(), archivedArtifact.getArtifactId());
        results.add(convertArtifactToMetadata(artifact, archivedMetadataUrl));
      });
    }
  }

  private Metadata convertArtifactToMetadata(Artifact artifact, String metadataUrl) {
    String artifactName = artifact.getName();
    if (StringUtils.isBlank(artifactName)) {
      artifactName = GitHubUtils.convertArtifactIdToName(artifact.getArtifactId());
    }
    String type = StringUtils.defaultIfBlank(artifact.getType(), ProductJsonConstants.DEFAULT_PRODUCT_TYPE);
    artifactName = String.format(MavenConstants.ARTIFACT_NAME_FORMAT, artifactName, type);
    return Metadata.builder().groupId(artifact.getGroupId()).versions(new ArrayList<>()).artifactId(
        artifact.getArtifactId()).metadataUrl(metadataUrl).repoUrl(
        StringUtils.defaultIfEmpty(artifact.getRepoUrl(), MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL)).type(type).name(
        artifactName).build();
  }

  private String buildMetadataUrlFromArtifactInfo(String repoUrl, String groupId, String artifactId) {
    if (StringUtils.isAnyBlank(groupId, artifactId)) {
      return StringUtils.EMPTY;
    }
    repoUrl = Optional.ofNullable(repoUrl).orElse(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL);
    groupId = groupId.replace(CommonConstants.DOT_SEPARATOR, CommonConstants.SLASH);
    return String.join(CommonConstants.SLASH, repoUrl, groupId, artifactId, MavenConstants.METADATA_URL_POSTFIX);
  }
}
