package com.axonivy.market.service.impl;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.constants.ReadmeConstants;
import com.axonivy.market.entity.*;
import com.axonivy.market.model.MavenArtifactModel;
import com.axonivy.market.repository.*;
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.service.ImageService;
import com.axonivy.market.service.MetadataService;
import com.axonivy.market.service.ProductJsonContentService;
import com.axonivy.market.util.MavenUtils;
import com.axonivy.market.util.MetadataReaderUtils;
import com.axonivy.market.util.ProductContentUtils;
import com.axonivy.market.util.VersionUtils;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
@Log4j2
public class MetadataServiceImpl implements MetadataService {
  private final ProductRepository productRepo;
  private final MetadataSyncRepository metadataSyncRepo;
  private final ProductJsonContentRepository productJsonRepo;
  private final MavenArtifactVersionRepository mavenArtifactVersionRepo;
  private final MetadataRepository metadataRepo;
  private final ImageService imageService;
  private final ProductJsonContentService productJsonContentService;
  private final FileDownloadService fileDownloadService;
  private final ProductModuleContentRepository productContentRepo;


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

  public void updateMavenArtifactVersionData(String productId, List<String> releasedVersions,
      Set<Metadata> metadataSet, MavenArtifactVersion artifactVersionCache) {
    for (Metadata metadata : metadataSet) {
      String metadataContent = MavenUtils.getMetadataContentFromUrl(metadata.getUrl());
      if (StringUtils.isBlank(metadataContent)) {
        continue;
      }
      Metadata metadataWithVersions = MetadataReaderUtils.updateMetadataFromMavenXML(metadataContent, metadata, false);
      updateMavenArtifactVersionFromMetadata(artifactVersionCache, metadataWithVersions);
      updateContentsFromNonMatchVersions(productId, releasedVersions, metadataWithVersions);
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
    updateMavenArtifactVersionData(productId, product.getReleasedVersions(), metadataSet, artifactVersionCache);

    // Persist changed
    metadataSyncRepo.save(syncCache);
    mavenArtifactVersionRepo.save(artifactVersionCache);
    metadataRepo.saveAll(metadataSet);
    return true;
  }

  public void updateContentsFromNonMatchVersions(String productId, List<String> releasedVersions,
      Metadata metadata) {
    List<ProductModuleContent> productModuleContents = new ArrayList<>();
    Set<String> nonMatchSnapshotVersions = getNonMatchSnapshotVersions(productId, releasedVersions,
        metadata.getVersions());

    for (String nonMatchSnapshotVersion : nonMatchSnapshotVersions) {
      if (MavenUtils.isProductArtifactId(metadata.getArtifactId())) {
        handleProductArtifact(metadata.getProductId(), nonMatchSnapshotVersion, metadata, productModuleContents);
      }
    }
    if (ObjectUtils.isNotEmpty(productModuleContents)) {
      productContentRepo.saveAll(productModuleContents);
    }
  }

  public void handleProductArtifact(String productId, String nonMatchSnapshotVersion, Metadata productArtifact,
      List<ProductModuleContent> productModuleContents) {
    Metadata snapShotMetadata = MavenUtils.buildSnapShotMetadataFromVersion(productArtifact, nonMatchSnapshotVersion);
    MetadataReaderUtils.updateMetadataFromMavenXML(
        MavenUtils.getMetadataContentFromUrl(snapShotMetadata.getUrl()), snapShotMetadata, true);

    String url = buildProductFolderDownloadUrl(snapShotMetadata, nonMatchSnapshotVersion);

    Product product = productRepo.findById(productId).orElse(null);
    if (StringUtils.isBlank(url) || Objects.isNull(product)) {
      return;
    }

    try {
      addProductContent(product, nonMatchSnapshotVersion, snapShotMetadata, url, productModuleContents);
    } catch (Exception e) {
      log.error("Cannot download and unzip file {}", e.getMessage());
    }
  }

  public String buildProductFolderDownloadUrl(Metadata snapShotMetadata, String nonMatchSnapshotVersion) {
    return MavenUtils.buildDownloadUrl(
        snapShotMetadata.getArtifactId(), nonMatchSnapshotVersion,
        MavenConstants.DEFAULT_PRODUCT_FOLDER_TYPE,
        snapShotMetadata.getRepoUrl(), snapShotMetadata.getGroupId(),
        snapShotMetadata.getSnapshotVersionValue());
  }

  private void addProductContent(Product product, String nonMatchSnapshotVersion, Metadata snapShotMetadata, String url,
      List<ProductModuleContent> productModuleContents) {
    ProductModuleContent productModuleContent = getReadmeAndProductContentsFromTag(product, nonMatchSnapshotVersion,
        snapShotMetadata, url);
    if (Objects.nonNull(productModuleContent)) {
      productModuleContents.add(productModuleContent);
    }
  }

  public Set<String> getNonMatchSnapshotVersions(String productId, List<String> releasedVersions,
      Set<String> metaVersions) {
    Set<String> nonMatchSnapshotVersions = new HashSet<>();
    for (String metaVersion : metaVersions) {
      String matchedVersion = VersionUtils.getMavenVersionMatchWithTag(releasedVersions, metaVersion);

      updateProductJsonAndReadmeContents(productId, metaVersion, matchedVersion);
      if (matchedVersion == null && VersionUtils.isSnapshotVersion(metaVersion)) {
        nonMatchSnapshotVersions.add(metaVersion);
      }
    }
    return nonMatchSnapshotVersions;
  }

  private void updateProductJsonAndReadmeContents(String productId, String metaVersion, String matchedVersion) {
    if (StringUtils.isNotBlank(matchedVersion)) {
      // Clone new record from matchVersion's values
      productJsonRepo.findByProductIdAndVersion(productId,
          matchedVersion).stream().findAny().ifPresent(json ->
          productRepo.findById(productId).ifPresent(product ->
              productJsonContentService.updateProductJsonContent(json.getContent(), null, metaVersion,
                  matchedVersion, product)
          )
      );

      // Note metaVersion that get matchTag's contents to display
      ProductModuleContent moduleContent =
          productContentRepo.findByTagAndProductId(VersionUtils.convertVersionToTag(productId, matchedVersion),
              productId);
      if (ObjectUtils.isEmpty(moduleContent)) {
        return;
      }
      Set<String> mavenVersions = Optional.ofNullable(moduleContent.getMavenVersions()).orElse(new HashSet<>());
      if (!mavenVersions.contains(metaVersion)) {
        mavenVersions.add(metaVersion);
        moduleContent.setMavenVersions(mavenVersions);
        productContentRepo.save(moduleContent);
      }
    }
  }

  private ProductModuleContent getReadmeAndProductContentsFromTag(Product product, String nonMatchSnapshotVersion,
      Metadata snapShotMetadata, String url) {
    ProductModuleContent productModuleContent = ProductContentUtils.initProductModuleContent(product.getId(), Strings.EMPTY,
        Set.of(nonMatchSnapshotVersion));
    String unzippedFolderPath = Strings.EMPTY;
    try {
      unzippedFolderPath = fileDownloadService.downloadAndUnzipProductContentFile(url, snapShotMetadata);
      updateDependencyContentsFromProductJson(productModuleContent, product, unzippedFolderPath);
      extractReadMeFileFromContents(product.getId(), unzippedFolderPath, productModuleContent);
    } catch (Exception e) {
      log.error("Cannot get product.json content in {}", e.getMessage());
      return null;
    } finally {
      if (StringUtils.isNotBlank(unzippedFolderPath)) {
        fileDownloadService.deleteDirectory(Path.of(unzippedFolderPath));
      }
    }
    return productModuleContent;
  }

  private void updateDependencyContentsFromProductJson(ProductModuleContent productModuleContent,
      Product product, String unzippedFolderPath) throws IOException {
    List<Artifact> artifacts = MavenUtils.convertProductJsonToMavenProductInfo(
        Paths.get(unzippedFolderPath));
    ProductContentUtils.updateProductModule(productModuleContent, artifacts);
    String currentVersion = productModuleContent.getMavenVersions().stream().findAny().orElse(null);
    Path productJsonPath = Paths.get(unzippedFolderPath, ProductJsonConstants.PRODUCT_JSON_FILE);
    String content = extractProductJsonContent(productJsonPath);
    productJsonContentService.updateProductJsonContent(content, null, currentVersion,
        ProductJsonConstants.VERSION_VALUE, product);
  }

  private void extractReadMeFileFromContents(String productId, String unzippedFolderPath,
      ProductModuleContent productModuleContent) {
    try {
      List<Path> readmeFiles;
      Map<String, Map<String, String>> moduleContents = new HashMap<>();
      try (Stream<Path> readmePathStream = Files.walk(Paths.get(unzippedFolderPath))) {
        readmeFiles = readmePathStream.filter(Files::isRegularFile).filter(
            path -> path.getFileName().toString().startsWith(ReadmeConstants.README_FILE_NAME)).toList();
      }
      if (ObjectUtils.isNotEmpty(readmeFiles)) {
        for (Path readmeFile : readmeFiles) {
          String readmeContents = Files.readString(readmeFile);
          if (ProductContentUtils.hasImageDirectives(readmeContents)) {
            readmeContents = updateImagesWithDownloadUrl(productId, unzippedFolderPath, readmeContents);
          }
          ProductContentUtils.getExtractedPartsOfReadme(moduleContents, readmeContents,
              readmeFile.getFileName().toString());
        }
        ProductContentUtils.updateProductModuleTabContents(productModuleContent, moduleContents);
      }
    } catch (Exception e) {
      log.error("Cannot get README file's content from folder {}: {}", unzippedFolderPath, e.getMessage());
    }
  }

  private String updateImagesWithDownloadUrl(String productId, String unzippedFolderPath,
      String readmeContents) throws IOException {
    List<Path> allImagePaths;
    Map<String, String> imageUrls = new HashMap<>();
    try (Stream<Path> imagePathStream = Files.walk(Paths.get(unzippedFolderPath))) {
      allImagePaths = imagePathStream.filter(Files::isRegularFile).filter(
          path -> path.getFileName().toString().toLowerCase().matches(CommonConstants.IMAGE_EXTENSION)).toList();
    }
    allImagePaths.forEach(
        imagePath -> Optional.of(imageService.mappingImageFromDownloadedFolder(productId, imagePath)).ifPresent(
            image -> imageUrls.put(imagePath.getFileName().toString(),
                CommonConstants.IMAGE_ID_PREFIX.concat(image.getId()))));

    return ProductContentUtils.replaceImageDirWithImageCustomId(imageUrls, readmeContents);
  }

  private String extractProductJsonContent(Path filePath) {
    try {
      InputStream contentStream = MavenUtils.extractedContentStream(filePath);
      return IOUtils.toString(Objects.requireNonNull(contentStream), StandardCharsets.UTF_8);
    } catch (Exception e) {
      log.error("Cannot extract product.json file {}", e.getMessage());
      return null;
    }
  }

  public void updateMavenArtifactVersionFromMetadata(MavenArtifactVersion artifactVersionCache,
      Metadata metadata) {
    // Skip to add new model for product artifact
    if (MavenUtils.isProductArtifactId(metadata.getArtifactId())) {
      return;
    }
    metadata.getVersions().forEach(version -> {
      if (VersionUtils.isSnapshotVersion(version)) {
        if (VersionUtils.isOfficialVersionOrUnReleasedDevVersion(metadata.getVersions().stream().toList(), version)) {
          updateMavenArtifactVersionForNonReleaseDevVersion(artifactVersionCache, metadata, version);
        }
      } else {
        updateMavenArtifactVersionCacheWithModel(artifactVersionCache, version, metadata);
      }
    });
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
