package com.axonivy.market.service.impl;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.constants.ReadmeConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.service.ImageService;
import com.axonivy.market.service.ProductContentService;
import com.axonivy.market.service.ProductJsonContentService;
import com.axonivy.market.util.MavenUtils;
import com.axonivy.market.util.ProductContentUtils;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Log4j2
@Service
@AllArgsConstructor
public class ProductContentServiceImpl implements ProductContentService {
  private final FileDownloadService fileDownloadService;
  private final ProductJsonContentService productJsonContentService;
  private final ImageService imageService;

  public ProductModuleContent getReadmeAndProductContentsFromTag(Product product, String version, String url,
      Artifact artifact) {
    ProductModuleContent productModuleContent = ProductContentUtils.initProductModuleContent(product.getId(),
        version);
    String unzippedFolderPath = Strings.EMPTY;
    try {
      unzippedFolderPath = fileDownloadService.downloadAndUnzipProductContentFile(url, artifact);
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
    Path productJsonPath = Paths.get(unzippedFolderPath, ProductJsonConstants.PRODUCT_JSON_FILE);
    String content = MavenUtils.extractProductJsonContent(productJsonPath);
    productJsonContentService.updateProductJsonContent(content, null, productModuleContent.getTag(),
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
}
