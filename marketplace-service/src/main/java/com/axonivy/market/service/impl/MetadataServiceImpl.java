package com.axonivy.market.service.impl;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.MetadataSync;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.bo.Artifact;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.service.MetadataService;
import com.axonivy.market.util.MavenUtils;
import com.axonivy.market.util.MetadataReaderUtils;
import com.axonivy.market.model.MavenArtifactModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.MetadataSyncRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class MetadataServiceImpl implements MetadataService {
  private final ProductRepository productRepo;
  private final MetadataSyncRepository metadataRepo;
  private final ProductJsonContentRepository productJsonRepo;
  private final MavenArtifactVersionRepository mavenArtifactVersionRepo;
  private final RestTemplate restTemplate = new RestTemplate();

  public MetadataServiceImpl(ProductRepository productRepo, MetadataSyncRepository metadataRepo,
      ProductJsonContentRepository productJsonRepo, MavenArtifactVersionRepository mavenArtifactVersionRepo) {
    this.productRepo = productRepo;
    this.metadataRepo = metadataRepo;
    this.productJsonRepo = productJsonRepo;
    this.mavenArtifactVersionRepo = mavenArtifactVersionRepo;
  }

  private static void updateMavenArtifactVersion(String version, Set<Metadata> metadataSet,
      MavenArtifactVersion artifactVersion, String productId) {
    metadataSet.forEach(metadata -> {
      if (metadata.getVersions().contains(version)) {
        MavenArtifactModel model = MavenArtifactModel.builder().name(metadata.getName()).downloadUrl(
            MavenUtils.buildDownloadUrl(metadata, version)).isInvalidArtifact(
            !metadata.getArtifactId().contains(productId)).build();
        artifactVersion.getProductArtifactWithVersionReleased().computeIfAbsent(version, k -> new ArrayList<>()).add(
            model);
      }
    });
  }

  @Override
  public boolean syncAllArtifactFromMaven() {
    boolean isAlreadyUpToDate = true;
    List<Product> products = productRepo.getAllProductWithIdAndReleaseTag();
    for (Product product : products) {
      Set<Artifact> artifacts = new HashSet<>();
      MetadataSync cache = metadataRepo.findById(product.getId()).orElse(
          MetadataSync.builder().productId(product.getId()).syncedTags(new HashSet<>()).build());
      List<String> nonSyncedVersions = getNonSyncedVersions(product, cache);
      if (CollectionUtils.isEmpty(nonSyncedVersions)) {
        continue;
      }
      isAlreadyUpToDate = false;
      updateArtifactsFromNonSyncedVersion(product, nonSyncedVersions, artifacts);

      Set<Metadata> metadataSet = convertArtifactsToMetadataSet(artifacts);
      MavenArtifactVersion artifactVersionCache = updateMavenArtifactVersionData(product, metadataSet,
          nonSyncedVersions);
      cache.getSyncedTags().addAll(nonSyncedVersions);
      metadataRepo.save(cache);
      mavenArtifactVersionRepo.save(artifactVersionCache);
    }
    return isAlreadyUpToDate;
  }

  @Override
  public void clearAllSync() {
    metadataRepo.deleteAll();
  }

  private MavenArtifactVersion updateMavenArtifactVersionData(Product product, Set<Metadata> metadataSet,
      List<String> nonSyncedVersions) {
    metadataSet.forEach(
        metadata -> MetadataReaderUtils.parseMetadataFromString(
            restTemplate.getForObject(metadata.getUrl(), String.class),
            metadata));
    MavenArtifactVersion artifactVersionCache = mavenArtifactVersionRepo.findById(product.getId()).orElse(
        new MavenArtifactVersion(product.getId(), new HashMap<>()));
    nonSyncedVersions.forEach(
        version -> updateMavenArtifactVersion(version, metadataSet, artifactVersionCache, product.getId()));
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

  private List<String> getNonSyncedVersions(Product product, MetadataSync cache) {
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
