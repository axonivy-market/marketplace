package com.axonivy.market.service.impl;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.constants.ReadmeConstants;
import com.axonivy.market.entity.Image;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.enums.Language;
import com.axonivy.market.model.ReadmeContentsModel;
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.service.ImageService;
import com.axonivy.market.service.MetadataService;
import com.axonivy.market.service.ProductContentService;
import com.axonivy.market.service.ProductJsonContentService;
import com.axonivy.market.util.MavenUtils;
import com.axonivy.market.util.ProductContentUtils;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
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

import static com.axonivy.market.util.ProductContentUtils.*;

@Log4j2
@Service
@AllArgsConstructor
public class ProductContentServiceImpl implements ProductContentService {
  private final FileDownloadService fileDownloadService;
  private final ProductJsonContentService productJsonContentService;
  private final ImageService imageService;
  private final MetadataService metadataService;

  @Override
  public ProductModuleContent getReadmeAndProductContentsFromVersion(String productId, String version, String url,
      Artifact artifact, String productName) {
    ProductModuleContent productModuleContent = ProductContentUtils.initProductModuleContent(productId, version);
    String unzippedFolderPath = Strings.EMPTY;
    try {
      unzippedFolderPath = fileDownloadService.downloadAndUnzipProductContentFile(url, artifact);
      updateDependencyContentsFromProductJson(productModuleContent, productId,
          unzippedFolderPath, productName, artifact);
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

    metadataService.updateArtifactAndMetaDataForProductJsonContent(productJsonContent, artifact);
  }
  private void extractReadMeFileFromContents(String productId, String unzippedFolderPath,
      ProductModuleContent productModuleContent) {
    Map<String, Map<String, String>> moduleContents = new HashMap<>();
    try (Stream<Path> readmePathStream = Files.walk(Paths.get(unzippedFolderPath))){
      List<Path> readmeFiles  = readmePathStream.filter(Files::isRegularFile)
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
}
