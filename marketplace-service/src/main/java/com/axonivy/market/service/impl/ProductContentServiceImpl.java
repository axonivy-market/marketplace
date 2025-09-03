package com.axonivy.market.service.impl;

import com.axonivy.market.bo.DownloadOption;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.constants.ReadmeConstants;
import com.axonivy.market.entity.Artifact;
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
import com.axonivy.market.util.FileUtils;
import com.axonivy.market.util.MavenUtils;
import com.axonivy.market.util.ProductContentUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Log4j2
@Service
@RequiredArgsConstructor
public class ProductContentServiceImpl implements ProductContentService {

  public static final Pattern IMAGE_EXTENSION_PATTERN =
      Pattern.compile(CommonConstants.IMAGE_EXTENSION);
  private final FileDownloadService fileDownloadService;
  private final ProductJsonContentService productJsonContentService;
  private final ImageService imageService;
  private final ProductDependencyRepository productDependencyRepository;
  private final ProductMarketplaceDataService productMarketplaceDataService;

  @Override
  public ProductModuleContent getReadmeAndProductContentsFromVersion(String productId, String version, String url,
      Artifact artifact, String productName) {
    var productModuleContent = ProductContentUtils.initProductModuleContent(productId, version);
    var unzippedFolderPath = String.join(File.separator, FileDownloadService.ROOT_STORAGE_FOR_PRODUCT_CONTENT,
        artifact.getArtifactId());
    try {

      unzippedFolderPath = fileDownloadService.downloadAndUnzipFile(url, DownloadOption.builder().isForced(true)
          .workingDirectory(unzippedFolderPath).shouldGrantPermission(false).build());
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
    var productJsonPath = Paths.get(unzippedFolderPath, ProductJsonConstants.PRODUCT_JSON_FILE);
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
        var readmeContents = Files.readString(readmeFile);
        if (ProductContentUtils.hasImageDirectives(readmeContents)) {
          readmeContents = updateImagesWithDownloadUrl(productId, unzippedFolderPath, readmeContents);
        }

        var readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(readmeContents);

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
      List<Path> allImagePaths = imagePathStream
          .filter(Files::isRegularFile)
          .filter(path -> IMAGE_EXTENSION_PATTERN
              .matcher(path.getFileName().toString().toLowerCase())
              .matches())
          .toList();

      for (Path imagePath : allImagePaths) {
        var currentImage = imageService.mappingImageFromDownloadedFolder(productId, imagePath);
        Optional.ofNullable(currentImage).ifPresent((Image image) -> {
          var imageFileName = imagePath.getFileName().toString();
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
  public List<String> getDependencyUrls(String productId, String artifactId, String version) {
    List<ProductDependency> productDependencies = productDependencyRepository.findByProductIdAndArtifactIdAndVersion(
        productId, artifactId, version);
    return Stream.concat(productDependencies.stream().map(ProductDependency::getDownloadUrl),
        productDependencies.stream().flatMap(product -> product.getDependencies().stream()).map(
            ProductDependency::getDownloadUrl)).toList();
  }

  @Override
  public void buildArtifactZipStreamFromUrls(String productId, List<String> urls, OutputStream out) {
    FileUtils.buildArtifactStreamFromArtifactUrls(urls, out);
    if (ObjectUtils.isNotEmpty(urls)) {
      productMarketplaceDataService.updateInstallationCountForProduct(productId, null);
    }
  }
}
