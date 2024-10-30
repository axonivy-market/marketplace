package com.axonivy.market.service.impl;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.constants.ReadmeConstants;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.model.MavenArtifactModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.MetadataRepository;
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.service.ImageService;
import com.axonivy.market.service.ProductContentService;
import com.axonivy.market.service.ProductJsonContentService;
import com.axonivy.market.util.MavenUtils;
import com.axonivy.market.util.MetadataReaderUtils;
import com.axonivy.market.util.ProductContentUtils;
import com.axonivy.market.util.VersionUtils;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Log4j2
@Service
@AllArgsConstructor
public class ProductContentServiceImpl implements ProductContentService {
  private final FileDownloadService fileDownloadService;
  private final ProductJsonContentService productJsonContentService;
  private final ImageService imageService;
  private final MetadataRepository metadataRepo;

  private final MavenArtifactVersionRepository mavenArtifactVersionRepo;

  @Override
  public ProductModuleContent getReadmeAndProductContentsFromVersion(String productId, String version, String url,
      Artifact artifact, String productName) {
    ProductModuleContent productModuleContent = ProductContentUtils.initProductModuleContent(productId,
        version);
    String unzippedFolderPath = Strings.EMPTY;
    try {
      unzippedFolderPath = fileDownloadService.downloadAndUnzipProductContentFile(url, artifact);
      updateDependencyContentsFromProductJson(productModuleContent, productId, unzippedFolderPath, productName , artifact);
      extractReadMeFileFromContents(productId, unzippedFolderPath, productModuleContent);
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

  public void updateDependencyContentsFromProductJson(ProductModuleContent productModuleContent,
      String productId, String unzippedFolderPath, String productName, Artifact artifact) throws IOException {
    List<Artifact> artifacts = MavenUtils.convertProductJsonToMavenProductInfo(
        Paths.get(unzippedFolderPath));
    ProductContentUtils.updateProductModule(productModuleContent, artifacts);
    Path productJsonPath = Paths.get(unzippedFolderPath, ProductJsonConstants.PRODUCT_JSON_FILE);
    String content = MavenUtils.extractProductJsonContent(productJsonPath);
    ProductJsonContent productJsonContent = productJsonContentService.updateProductJsonContent(content,
        productModuleContent.getVersion(),
        ProductJsonConstants.VERSION_VALUE, productId, productName);

    updateArtifactAndMetaDataForProduct(productJsonContent,artifact);
  }

  private void updateArtifactAndMetaDataForProduct(ProductJsonContent productJsonContent , Artifact productArtifact) {
    if (ObjectUtils.isEmpty(productJsonContent)) {
      return;
    }

    List<Artifact> artifactsInVersion = MavenUtils.getMavenArtifactsFromProductJson(productJsonContent);
    Set<Metadata> metadataSet = new HashSet<>();
    for (Artifact artifact : artifactsInVersion) {
      String metadataUrl = MavenUtils.buildMetadataUrlFromArtifactInfo(artifact.getRepoUrl(), artifact.getGroupId(),
          artifact.getArtifactId());
      metadataSet.add(MavenUtils.convertArtifactToMetadata(productJsonContent.getProductId(), artifact, metadataUrl));
      metadataSet.addAll(MavenUtils.extractMetaDataFromArchivedArtifacts(productJsonContent.getProductId(), artifact));
    }

    if (ObjectUtils.isNotEmpty(productArtifact)) {
      metadataSet.addAll(MavenUtils.convertArtifactsToMetadataSet(Set.of(productArtifact),
          productJsonContent.getProductId()));
    }

    if (CollectionUtils.isEmpty(metadataSet)) {
      log.info("**MetadataService: No artifact found in product {}", productJsonContent.getProductId());
      return;
    }

    MavenArtifactVersion artifactVersionCache = mavenArtifactVersionRepo.findById(productJsonContent.getProductId())
        .orElse(MavenArtifactVersion.builder().productId(productJsonContent.getProductId()).build());

    artifactVersionCache.setAdditionalArtifactsByVersion(new HashMap<>());
    updateMavenArtifactVersionData(metadataSet, artifactVersionCache);

    mavenArtifactVersionRepo.save(artifactVersionCache);
    metadataRepo.saveAll(metadataSet);
  }

  public void updateMavenArtifactVersionData(Set<Metadata> metadataSet, MavenArtifactVersion artifactVersionCache) {
    for (Metadata metadata : metadataSet) {
      String metadataContent = MavenUtils.getMetadataContentFromUrl(metadata.getUrl());
      if (StringUtils.isBlank(metadataContent)) {
        continue;
      }
      Metadata metadataWithVersions = MetadataReaderUtils.updateMetadataFromMavenXML(metadataContent, metadata, false);
      updateMavenArtifactVersionFromMetadata(artifactVersionCache, metadataWithVersions);
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

  public String updateImagesWithDownloadUrl(String productId, String unzippedFolderPath,
      String readmeContents) throws IOException {
    List<Path> allImagePaths;
    Map<String, String> imageUrls = new HashMap<>();
    try (Stream<Path> imagePathStream = Files.walk(Paths.get(unzippedFolderPath))) {
      allImagePaths = imagePathStream.filter(Files::isRegularFile).filter(
          path -> path.getFileName().toString().toLowerCase().matches(CommonConstants.IMAGE_EXTENSION)).toList();
    }
    for (Path imagePath : allImagePaths) {
      Optional.of(imageService.mappingImageFromDownloadedFolder(productId, imagePath)).ifPresent(
          image -> imageUrls.put(imagePath.getFileName().toString(),
              CommonConstants.IMAGE_ID_PREFIX.concat(image.getId())));
    }

    return ProductContentUtils.replaceImageDirWithImageCustomId(imageUrls, readmeContents);
  }
}
