package com.axonivy.market.service.impl;

import com.axonivy.market.comparator.ArchivedArtifactsComparator;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.github.model.ArchivedArtifact;
import com.axonivy.market.maven.model.Artifact;
import com.axonivy.market.maven.util.MavenUtils;
import com.axonivy.market.model.MavenArtifactModel;
import com.axonivy.market.model.MavenArtifactVersionModel;
import com.axonivy.market.model.VersionAndUrlModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductModuleContentRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.VersionService;
import com.axonivy.market.util.VersionUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

  public static Map<String, List<ArchivedArtifact>> getArchivedArtifactMapFromMeta(
      List<Artifact> artifactsFromMeta) {
    Map<String, List<ArchivedArtifact>> result = new HashMap<>();
    artifactsFromMeta.forEach(artifact -> {
      List<ArchivedArtifact> archivedArtifacts = new ArrayList<>(
          Optional.ofNullable(artifact.getArchivedArtifacts()).orElse(Collections.emptyList()).stream()
              .sorted(new ArchivedArtifactsComparator()).toList());
      Collections.reverse(archivedArtifacts);
      result.put(artifact.getArtifactId(), archivedArtifacts);
    });
    return result;
  }

  public List<MavenArtifactVersionModel> getArtifactsAndVersionToDisplay(String productId, Boolean isShowDevVersion,
      String designerVersion) {
    Map<String, List<MavenArtifactModel>> cache = mavenArtifactVersionRepository.findById(productId).orElse(
        new com.axonivy.market.entity.MavenArtifactVersion(productId)).getProductArtifactWithVersionReleased();
    List<String> versionsToDisplay = VersionUtils.getVersionsToDisplay(new ArrayList<>(cache.keySet()),
        isShowDevVersion, designerVersion);

    List<Artifact> artifactsFromMeta = getArtifactsFromMeta(productId).stream()
        .filter(artifact -> !artifact.getArtifactId().endsWith(MavenConstants.PRODUCT_ARTIFACT_POSTFIX)).toList();
    List<MavenArtifactVersionModel> results = new ArrayList<>();

    for (String version : versionsToDisplay) {
      results.add(new MavenArtifactVersionModel(version, cache.get(version)));
    }
    return results;
  }

  public Map<String, Object> getProductJsonContentByIdAndVersion(String productId, String version) {
    Map<String, Object> result = new HashMap<>();
    try {
      ProductJsonContent productJsonContent = productJsonRepo.findByProductIdAndVersion(productId, version);
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

  public List<Artifact> getArtifactsFromMeta(String productId) {
    Product productInfo = productRepo.findById(productId).orElse(new Product());
    return Optional.ofNullable(productInfo.getArtifacts()).orElse(new ArrayList<>());
  }

  public List<String> getPersistedVersions(String productId) {
    var product = productRepo.findById(productId);
    Set<String> versions = new HashSet<>();
    if (product.isPresent()) {
      versions.addAll(product.get().getReleasedVersions());
    }
    if (CollectionUtils.isEmpty(versions)) {
      versions.addAll(productContentRepo.findTagsByProductId(productId));
      versions = versions.stream().map(VersionUtils::convertTagToVersion).collect(Collectors.toSet());
    }
    return new ArrayList<>(versions);
  }

  public List<Artifact> getMavenArtifactsFromProductJsonByVersion(String version,
      String productId) {
    ProductJsonContent productJson = productJsonRepo.findByProductIdAndVersion(productId, version);
    return MavenUtils.getMavenArtifactsFromProductJson(productJson);
  }
}
