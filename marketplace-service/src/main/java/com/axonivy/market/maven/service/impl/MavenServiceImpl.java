package com.axonivy.market.maven.service.impl;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.maven.model.Artifact;
import com.axonivy.market.maven.model.Metadata;
import com.axonivy.market.entity.MavenVersionSync;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.maven.util.MavenUtils;
import com.axonivy.market.model.MavenArtifactModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.MavenVersionSyncRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.maven.service.MavenService;
import com.axonivy.market.util.XmlReaderUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

  @Override
  public void syncAllArtifactFromMaven() {
    List<Product> products = productRepo.getAllProductWithIdAndReleaseTag();
    products.forEach(product -> {
      Set<Artifact> artifactFromProduct = new HashSet<>();
      //TODO: build url
      String productArtifactMetaUrl = "";
      List<String> nonSyncedVersions = new ArrayList<>();
      XmlReaderUtils.extractVersions(productArtifactMetaUrl, nonSyncedVersions);
      MavenVersionSync mavenVersionCache = mavenRepo.findById(product.getId()).orElse(new MavenVersionSync());
      // remove version handled
      if (!CollectionUtils.isEmpty(mavenVersionCache.getSyncedVersions())) {
        nonSyncedVersions.removeAll(mavenVersionCache.getSyncedVersions());
      }
      //get all artifact from product json
      nonSyncedVersions.forEach(version -> {
        ProductJsonContent productJson = productJsonRepo.findByProductIdAndVersion(product.getId(), version);
        List<Artifact> artifactsInVersion = MavenUtils.getMavenArtifactsFromProductJson(productJson);
        artifactFromProduct.addAll(artifactsInVersion);
      });

      // get all version of each artifact
      Set<Metadata> metadataSet = convertMavenArtifactsToMavenVersions(artifactFromProduct);
      metadataSet.forEach(version -> XmlReaderUtils.extractDataFromUrl(version.getMetadataUrl(), version));
      MavenArtifactVersion artifactVersion =
          mavenArtifactVersionRepo.findById(product.getId()).orElse(new MavenArtifactVersion());
      var artifactVersionCache = artifactVersion.getProductArtifactWithVersionReleased();
      nonSyncedVersions.forEach(version -> {
        metadataSet.forEach(m -> {
          MavenArtifactModel model =
              MavenArtifactModel.builder().name(m.getName()).downloadUrl("").build();
          artifactVersionCache.get(version).add(model);
        });
      });
    });

  }

  private Set<Metadata> convertMavenArtifactsToMavenVersions(Set<Artifact> artifacts) {
    Set<Metadata> results = new HashSet<>();
    if (!CollectionUtils.isEmpty(artifacts)) {
      artifacts.forEach(artifact -> {
        String metadataUrl = buildMavenMetadataUrlFromArtifact(artifact.getRepoUrl(), artifact.getGroupId(),
            artifact.getArtifactId());
        results.add(convertMavenArtifactToMavenVersion(artifact, metadataUrl));
        if (!CollectionUtils.isEmpty(artifact.getArchivedArtifacts())) {
          artifact.getArchivedArtifacts().forEach(archivedArtifact -> {
            String archivedMetadataUrl = buildMavenMetadataUrlFromArtifact(artifact.getRepoUrl(),
                archivedArtifact.getGroupId(),
                archivedArtifact.getArtifactId());
            results.add(convertMavenArtifactToMavenVersion(artifact, archivedMetadataUrl));
          });
        }
      });
    }
    return results;
  }

  private Metadata convertMavenArtifactToMavenVersion(Artifact artifact, String metadataUrl) {
    return Metadata.builder().groupId(artifact.getGroupId()).artifactId(artifact.getArtifactId()).metadataUrl(
        metadataUrl).build();
  }

  private String buildMavenMetadataUrlFromArtifact(String repoUrl, String groupId, String artifactID) {
    if (StringUtils.isAnyBlank(groupId, artifactID)) {
      return StringUtils.EMPTY;
    }
    repoUrl = Optional.ofNullable(repoUrl).orElse(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL);
    groupId = groupId.replace(CommonConstants.DOT_SEPARATOR, CommonConstants.SLASH);
    return String.join(CommonConstants.SLASH, repoUrl, groupId, artifactID, MavenConstants.METADATA_URL_POSTFIX);
  }
}
