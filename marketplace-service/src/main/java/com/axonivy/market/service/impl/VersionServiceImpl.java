package com.axonivy.market.service.impl;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.model.MavenArtifactModel;
import com.axonivy.market.model.MavenArtifactVersionModel;
import com.axonivy.market.model.VersionAndUrlModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductModuleContentRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.VersionService;
import com.axonivy.market.util.MavenUtils;
import com.axonivy.market.util.VersionUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.axonivy.market.constants.ProductJsonConstants.NAME;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Log4j2
@Service
public class VersionServiceImpl implements VersionService {

  private final MavenArtifactVersionRepository mavenArtifactVersionRepository;
  private final ProductRepository productRepo;
  private final ProductJsonContentRepository productJsonRepo;
  private final ProductModuleContentRepository productContentRepo;
  private final ObjectMapper mapper = new ObjectMapper();

  public VersionServiceImpl(
      MavenArtifactVersionRepository mavenArtifactVersionRepository, ProductRepository productRepo,
      ProductJsonContentRepository productJsonRepo, ProductModuleContentRepository productContentRepo) {
    this.mavenArtifactVersionRepository = mavenArtifactVersionRepository;
    this.productRepo = productRepo;
    this.productJsonRepo = productJsonRepo;
    this.productContentRepo = productContentRepo;
  }

  public List<MavenArtifactVersionModel> getArtifactsAndVersionToDisplay(String productId, Boolean isShowDevVersion,
      String designerVersion) {
    MavenArtifactVersion cache = mavenArtifactVersionRepository.findById(productId).orElse(
        MavenArtifactVersion.builder().productId(productId).build());
    List<String> versionsToDisplay = VersionUtils.getVersionsToDisplay(new ArrayList<>(cache.getProductArtifactsByVersion().keySet()),
        isShowDevVersion, designerVersion);

    List<Artifact> artifactsFromMeta = MavenUtils.filterNonProductArtifactFromMeta(getArtifactsFromMeta(productId));
    List<MavenArtifactVersionModel> results = new ArrayList<>();

    for (String mavenVersion : versionsToDisplay) {
      List<MavenArtifactModel> artifactsByVersion = new ArrayList<>();
      artifactsByVersion.addAll(MavenUtils.convertArtifactsToModels(artifactsFromMeta, mavenVersion));
      artifactsByVersion.addAll(
          cache.getProductArtifactsByVersion().computeIfAbsent(mavenVersion, k -> new ArrayList<>()));
      artifactsByVersion.addAll(
          cache.getAdditionalArtifactsByVersion().computeIfAbsent(mavenVersion, k -> new ArrayList<>()));

      List<String> releasedVersions = productRepo.getReleasedVersionsById(productId);
      String version = VersionUtils.getMavenVersionMatchWithTag(releasedVersions, mavenVersion);

      if (StringUtils.isNotBlank(version)) {
        ProductJsonContent json = productJsonRepo.findByProductIdAndVersion(productId, version);
        json.setRelatedMavenVersions(Set.of(mavenVersion));
        productJsonRepo.save(json);

        ProductModuleContent moduleContent =
            productContentRepo.findByTagAndProductId(VersionUtils.convertVersionToTag(productId, version), productId);
        moduleContent.setRelatedMavenVersions(Set.of(mavenVersion));
        productContentRepo.save(moduleContent);
      }

      if (!CollectionUtils.isEmpty(artifactsByVersion)) {
        results.add(new MavenArtifactVersionModel(version, artifactsByVersion));
      }
    }
    return results;
  }

  public Map<String, Object> getProductJsonContentByIdAndTag(String productId, String tag) {
    Map<String, Object> result = new HashMap<>();
    try {
      ProductJsonContent productJsonContent = productJsonRepo.findByProductIdAndVersion(productId, tag);
      if (ObjectUtils.isEmpty(productJsonContent)) {
        return new HashMap<>();
      }
      result = mapper.readValue(productJsonContent.getContent(), Map.class);
      result.computeIfAbsent(NAME, k -> productJsonContent.getName());
    } catch (JsonProcessingException jsonProcessingException) {
      log.error(jsonProcessingException.getMessage());
    }
    return result;
  }

  @Override
  public List<VersionAndUrlModel> getVersionsForDesigner(String productId) {
    List<VersionAndUrlModel> versionAndUrlList = new ArrayList<>();
    List<String> versions = productRepo.getReleasedVersionsById(productId);
    for (String version : versions) {
      Link link = linkTo(
          methodOn(ProductDetailsController.class).findProductJsonContent(productId, version)).withSelfRel();
      VersionAndUrlModel versionAndUrlModel = new VersionAndUrlModel(version, link.getHref());
      versionAndUrlList.add(versionAndUrlModel);
    }
    return versionAndUrlList;
  }

  @Override
  public void clearAllProductVersions() {
    mavenArtifactVersionRepository.deleteAll();
  }

  public List<Artifact> getArtifactsFromMeta(String productId) {
    Product productInfo = productRepo.findById(productId).orElse(new Product());
    return Optional.ofNullable(productInfo.getArtifacts()).orElse(new ArrayList<>());
  }

  public List<String> getPersistedVersions(String productId) {
    var product = productRepo.findById(productId);
    List<String> versions = new ArrayList<>();
    if (product.isPresent()) {
      versions.addAll(product.get().getReleasedVersions());
    }
    if (CollectionUtils.isEmpty(versions)) {
      versions.addAll(productContentRepo.findTagsByProductId(productId));
      versions = versions.stream().map(VersionUtils::convertTagToVersion).collect(Collectors.toList());
    }
    return versions;
  }

  public List<Artifact> getMavenArtifactsFromProductJsonByTag(String tag,
      String productId) {
    ProductJsonContent productJson = productJsonRepo.findByProductIdAndVersion(productId, tag);
    return MavenUtils.getMavenArtifactsFromProductJson(productJson);
  }
}
