package com.axonivy.market.service.impl;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.entity.MetadataSync;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.model.MavenArtifactModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.MetadataRepository;
import com.axonivy.market.repository.MetadataSyncRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.MetadataService;
import com.axonivy.market.util.MavenUtils;
import com.axonivy.market.util.MetadataReaderUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Log4j2
public class MetadataServiceImpl implements MetadataService {
  private final ProductRepository productRepo;
  private final MetadataSyncRepository metadataSyncRepo;
  private final ProductJsonContentRepository productJsonRepo;
  private final MavenArtifactVersionRepository mavenArtifactVersionRepo;
  private final MetadataRepository metadataRepo;
  private final RestTemplate restTemplate = new RestTemplate();


  public MetadataServiceImpl(ProductRepository productRepo, MetadataSyncRepository metadataSyncRepo,
      ProductJsonContentRepository productJsonRepo, MavenArtifactVersionRepository mavenArtifactVersionRepo,
      MetadataRepository metadataRepo) {
    this.productRepo = productRepo;
    this.metadataSyncRepo = metadataSyncRepo;
    this.productJsonRepo = productJsonRepo;
    this.mavenArtifactVersionRepo = mavenArtifactVersionRepo;
    this.metadataRepo = metadataRepo;
  }

  private static void updateMavenArtifactVersion(String productId, MavenArtifactVersion artifactVersionCache,
      Metadata metadata) {
    metadata.getVersions().forEach(version -> {
      MavenArtifactModel model = MavenArtifactModel.builder().name(metadata.getName()).downloadUrl(
          MavenUtils.buildDownloadUrl(metadata, version)).isInvalidArtifact(
          !metadata.getArtifactId().contains(productId)).build();
      boolean isExist = artifactVersionCache.getProductArtifactWithVersionReleased().computeIfAbsent(version,
          k -> new ArrayList<>()).stream().anyMatch(artifact -> StringUtils.equals(model.getDownloadUrl(),
          artifact.getDownloadUrl()));
      if (!isExist) {
        artifactVersionCache.getProductArtifactWithVersionReleased().get(version).add(model);
      }
    });
  }

  @Override
  public void clearAllSync() {
    metadataSyncRepo.deleteAll();
  }

  @Override
  public boolean syncAllArtifactFromMaven() {
    boolean isAlreadyUpToDate = true;
    List<Product> products = productRepo.getAllProductWithIdAndReleaseTag();
    log.info("**Sync maven version :: Start to sync version for {} product(s).**", products.size());
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
      List<String> nonSyncedVersionOfTags = getNonSyncedVersionOfTags(product, syncCache);
      if (!CollectionUtils.isEmpty(nonSyncedVersionOfTags)) {
        isAlreadyUpToDate = false;
        updateArtifactsFromNonSyncedVersion(product.getId(), nonSyncedVersionOfTags, artifactsFromNewTags);
      }

      // Sync versions from maven & update artifacts-version table
      metadataSet.addAll(convertArtifactsToMetadataSet(artifactsFromNewTags));
      updateMavenArtifactVersionData(product.getId(), metadataSet, artifactVersionCache);

      // Persist changed
      syncCache.getSyncedTags().addAll(nonSyncedVersionOfTags);
      metadataSyncRepo.save(syncCache);
      mavenArtifactVersionRepo.save(artifactVersionCache);
    }
    return isAlreadyUpToDate;
  }

  private void updateMavenArtifactVersionData(String productId, Set<Metadata> metadataSet,
      MavenArtifactVersion artifactVersionCache) {
    metadataSet.forEach(
        metadata -> {
          MetadataReaderUtils.parseMetadataFromString(restTemplate.getForObject(metadata.getUrl(), String.class),
              metadata);
          updateMavenArtifactVersion(productId, artifactVersionCache, metadata);
        });
  }

  private void updateArtifactsFromNonSyncedVersion(String productId, List<String> nonSyncedVersions,
      Set<Artifact> artifacts) {
    if (CollectionUtils.isEmpty(nonSyncedVersions)) {
      return;
    }
    nonSyncedVersions.forEach(tag -> {
      ProductJsonContent productJson = productJsonRepo.findByProductIdAndTag(productId, tag);
      List<Artifact> artifactsInVersion = MavenUtils.getMavenArtifactsFromProductJson(productJson);
      artifacts.addAll(artifactsInVersion);
    });
  }

  private List<String> getNonSyncedVersionOfTags(Product product, MetadataSync cache) {
    List<String> nonSyncedVersions = product.getReleasedVersions();
    if (!CollectionUtils.isEmpty(cache.getSyncedTags())) {
      nonSyncedVersions.removeAll(cache.getSyncedTags());
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
    return Metadata.builder().groupId(artifact.getGroupId()).versions(new HashSet<>()).artifactId(
        artifact.getArtifactId()).url(metadataUrl).repoUrl(
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