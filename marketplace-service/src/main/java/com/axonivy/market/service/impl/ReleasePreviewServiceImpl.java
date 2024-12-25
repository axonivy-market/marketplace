package com.axonivy.market.service.impl;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.ReadmeConstants;
import com.axonivy.market.model.ReadmeContentsModel;
import com.axonivy.market.model.ReleasePreview;
import com.axonivy.market.service.ReleasePreviewService;
import com.axonivy.market.util.FileUtils;
import com.axonivy.market.util.ProductContentUtils;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static com.axonivy.market.constants.PreviewConstants.IMAGE_DOWNLOAD_URL;
import static com.axonivy.market.constants.PreviewConstants.PREVIEW_DIR;

@Log4j2
@Service
@AllArgsConstructor
public class ReleasePreviewServiceImpl implements ReleasePreviewService {


  @Override
  public ReleasePreview extract(MultipartFile file, String baseUrl) {
    try {
      FileUtils.unzip(file, PREVIEW_DIR);
    } catch (IOException e){
      log.info("#extract Error extracting zip file, message: {}", e.getMessage());
    }
    return extractREADME(baseUrl, PREVIEW_DIR);
  }

  public ReleasePreview extractREADME(String baseUrl, String location) {
    Map<String, Map<String, String>> moduleContents = new HashMap<>();
    try (Stream<Path> readmePathStream = Files.walk(Paths.get(location))) {
      List<Path> readmeFiles = readmePathStream.filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().startsWith(ReadmeConstants.README_FILE_NAME))
          .toList();
      if (readmeFiles.isEmpty()) {
        return null;
      }
      for (Path readmeFile : readmeFiles) {
        processReadme(readmeFile, moduleContents, baseUrl, location);
      }
      return ReleasePreview.from(moduleContents);
    } catch (IOException e) {
      log.error("Cannot get README file's content from folder {}: {}",
          PREVIEW_DIR, e.getMessage());
      return null;
    }
  }

  public String updateImagesWithDownloadUrl(String unzippedFolderPath,
      String readmeContents, String baseUrl) {
    Map<String, String> imageUrls = new HashMap<>();
    try (Stream<Path> imagePathStream = Files.walk(Paths.get(unzippedFolderPath))) {
      List<Path> allImagePaths = imagePathStream.filter(Files::isRegularFile).filter(
          path -> path.getFileName().toString().toLowerCase().matches(CommonConstants.IMAGE_EXTENSION)).toList();

      allImagePaths.stream()
          .filter(Objects::nonNull)
          .forEach(imagePath -> {
            String imageFileName = imagePath.getFileName().toString();
            String downloadURLFormat = String.format(IMAGE_DOWNLOAD_URL, baseUrl, imageFileName);
            imageUrls.put(imageFileName, downloadURLFormat);
          });
      return ProductContentUtils.replaceImageDirWithImageCustomId(imageUrls, readmeContents);
    } catch (Exception e) {
      log.error("#updateImagesWithDownloadUrl: Error update image url: {}", e.getMessage());
      return null;
    }
  }

  public void processReadme(Path readmeFile, Map<String, Map<String, String>> moduleContents,
      String baseUrl, String location) throws IOException {
    String readmeContents = Files.readString(readmeFile);
    if (ProductContentUtils.hasImageDirectives(readmeContents)) {
      readmeContents = updateImagesWithDownloadUrl(location, readmeContents, baseUrl);
    }
    ReadmeContentsModel readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(readmeContents);
    ProductContentUtils.mappingDescriptionSetupAndDemo(
        moduleContents,
        readmeFile.getFileName().toString(),
        readmeContentsModel
    );
  }

}
