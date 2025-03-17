package com.axonivy.market.service.impl;

import com.axonivy.market.entity.Artifact;
import com.axonivy.market.bo.DownloadOption;
import com.axonivy.market.bo.MavenDependency;
import com.axonivy.market.bo.VersionDownload;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.constants.ReadmeConstants;
import com.axonivy.market.entity.Image;
import com.axonivy.market.entity.ProductDependency;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.model.ReadmeContentsModel;
import com.axonivy.market.repository.ProductDependencyRepository;
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.service.ImageService;
import com.axonivy.market.service.ProductContentService;
import com.axonivy.market.service.ProductJsonContentService;
import com.axonivy.market.service.ProductMarketplaceDataService;
import com.axonivy.market.util.MavenUtils;
import com.axonivy.market.util.ProductContentUtils;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Log4j2
@Service
@AllArgsConstructor
public class ProductContentServiceImpl implements ProductContentService {
  private final FileDownloadService fileDownloadService;
  private final ProductJsonContentService productJsonContentService;
  private final ImageService imageService;
  private final ProductMarketplaceDataService productMarketplaceDataService;
  private final ProductDependencyRepository productDependencyRepository;

  @Override
  public ProductModuleContent getReadmeAndProductContentsFromVersion(String productId, String version, String url,
      Artifact artifact, String productName) {
    ProductModuleContent productModuleContent = ProductContentUtils.initProductModuleContent(productId, version);
    String unzippedFolderPath = String.join(File.separator, FileDownloadService.ROOT_STORAGE_FOR_PRODUCT_CONTENT,
        artifact.getArtifactId());
    try {

      unzippedFolderPath = fileDownloadService.downloadAndUnzipFile(url, new DownloadOption(true, unzippedFolderPath));
      updateDependencyContentsFromProductJson(productModuleContent, productId, unzippedFolderPath, productName);
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
      String productId, String unzippedFolderPath, String productName) throws IOException {
    List<Artifact> artifacts = MavenUtils.convertProductJsonToMavenProductInfo(Paths.get(unzippedFolderPath));
    ProductContentUtils.updateProductModule(productModuleContent, artifacts);
    Path productJsonPath = Paths.get(unzippedFolderPath, ProductJsonConstants.PRODUCT_JSON_FILE);
    String content = MavenUtils.extractProductJsonContent(productJsonPath);

    productJsonContentService.updateProductJsonContent(content, productModuleContent.getVersion(),
        ProductJsonConstants.VERSION_VALUE, productId, productName);
  }

  private void extractReadMeFileFromContents(String productId, String unzippedFolderPath,
      ProductModuleContent productModuleContent) {
    Map<String, Map<String, String>> moduleContents = new HashMap<>();
    try (Stream<Path> readmePathStream = Files.walk(Paths.get(unzippedFolderPath))) {
      List<Path> readmeFiles = readmePathStream.filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().startsWith(ReadmeConstants.README_FILE_NAME))
          .toList();

      for (Path readmeFile : readmeFiles) {
        String readmeContents = Files.readString(readmeFile);
        if (ProductContentUtils.hasImageDirectives(readmeContents)) {
          readmeContents = updateImagesWithDownloadUrl(productId, unzippedFolderPath, readmeContents);
        }

        ReadmeContentsModel readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(readmeContents);

        ProductContentUtils.mappingDescriptionSetupAndDemo(moduleContents, readmeFile.getFileName().toString(),
            readmeContentsModel);
      }
      ProductContentUtils.updateProductModuleTabContents(productModuleContent, moduleContents);
    } catch (IOException e) {
      log.error("Cannot get README file's content from folder {}: {}", unzippedFolderPath, e.getMessage());
    }
  }

  public String updateImagesWithDownloadUrl(String productId, String unzippedFolderPath,
      String readmeContents) {
    Map<String, String> imageUrls = new HashMap<>();
    try (Stream<Path> imagePathStream = Files.walk(Paths.get(unzippedFolderPath))) {
      List<Path> allImagePaths = imagePathStream.filter(Files::isRegularFile).filter(
          path -> path.getFileName().toString().toLowerCase().matches(CommonConstants.IMAGE_EXTENSION)).toList();

      for (Path imagePath : allImagePaths) {
        Image currentImage = imageService.mappingImageFromDownloadedFolder(productId, imagePath);
        Optional.ofNullable(currentImage).ifPresent(image -> {
          String imageFileName = imagePath.getFileName().toString();
          String imageIdFormat = CommonConstants.IMAGE_ID_PREFIX.concat(image.getId());
          imageUrls.put(imageFileName, imageIdFormat);
        });
      }

      return ProductContentUtils.replaceImageDirWithImageCustomId(imageUrls, readmeContents);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return readmeContents;
  }

  @Override
  public VersionDownload downloadZipArtifactFile(String productId, String artifactId,
      String version) {
    List<MavenDependency> mavenDependencies = getMavenDependenciesOfProduct(productId, artifactId, version);
    if (ObjectUtils.isEmpty(mavenDependencies)) {
      return null;
    }
    // Create a ZIP file
    try {
      var byteArrayOutputStream = new ByteArrayOutputStream();
      try (var zipOut = new ZipOutputStream(byteArrayOutputStream)) {
        for (var mavenArtifact : mavenDependencies) {
          zipArtifact(version, mavenArtifact, zipOut);
          zipDependencyArtifacts(version, mavenArtifact, zipOut);
        }
        zipConfigurationOptions(zipOut);
        zipOut.closeEntry();
      }
      int installationCount = productMarketplaceDataService.updateInstallationCountForProduct(productId, null);
      String base64FileData = Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());

      return VersionDownload.builder()
          .fileData(base64FileData.getBytes())
          .installationCount(installationCount)
          .build();
    } catch (IOException e) {
      log.error("Cannot create ZIP file {}", e.getMessage());
      return null;
    }
  }

  private void zipDependencyArtifacts(String version, MavenDependency mavenArtifact, ZipOutputStream zipOut)
      throws IOException {
    if (mavenArtifact == null || ObjectUtils.isEmpty(mavenArtifact.getDependencies())) {
      return;
    }
    for (var dependency : Optional.ofNullable(mavenArtifact.getDependencies()).orElse(List.of())) {
      zipArtifact(version, dependency, zipOut);
    }
  }

  private List<MavenDependency> getMavenDependenciesOfProduct(String productId, String artifactId, String version) {
    Predicate<MavenDependency> filterByArtifactAndVersion =
        dependency -> dependency.getArtifactId().equals(artifactId) &&
            dependency.getVersion().equals(version);

    ProductDependency productDependency = productDependencyRepository.findByIdWithDependencies(productId);

    return Optional.ofNullable(productDependency)
        .map(ProductDependency::getDependenciesOfArtifact)
        .map(Collection::stream)
        .map(dependencies -> dependencies.filter(filterByArtifactAndVersion).toList())
        .orElse(new ArrayList<>());
  }

  private void zipConfigurationOptions(ZipOutputStream zipOut) throws IOException {
    final String configFile = "deploy.options.yaml";
    ClassPathResource resource = new ClassPathResource("app-zip/" + configFile);
    String content = Files.readString(Path.of(resource.getURI()), StandardCharsets.UTF_8);
    addNewFileToZip(configFile, zipOut, content.getBytes());
  }

  private static void addNewFileToZip(String fileName, ZipOutputStream zipOut, byte[] content) throws IOException {
    ZipEntry entry = new ZipEntry(fileName);
    zipOut.putNextEntry(entry);
    zipOut.write(content);
    zipOut.closeEntry();
  }

  private void zipArtifact(String version, MavenDependency mavenArtifact, ZipOutputStream zipOut) throws IOException {
    if (mavenArtifact == null || StringUtils.isBlank(mavenArtifact.getDownloadUrl())) {
      return;
    }
    byte[] artifactData = fileDownloadService.downloadFile(mavenArtifact.getDownloadUrl());
    String filename = StringUtils.substringAfter(mavenArtifact.getDownloadUrl(), String.format("/%s/", version));
    addNewFileToZip(filename, zipOut, artifactData);
  }
}
