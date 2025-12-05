package com.axonivy.market.service.impl;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.ReadmeConstants;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.exceptions.model.FileProcessingException;
import com.axonivy.market.model.ReleasePreview;
import com.axonivy.market.service.ReleasePreviewService;
import com.axonivy.market.util.FileUtils;
import com.axonivy.market.util.ProductContentUtils;
import com.axonivy.market.util.ZipSafetyScanner;
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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.axonivy.market.constants.PreviewConstants.IMAGE_DOWNLOAD_URL;
import static com.axonivy.market.constants.PreviewConstants.PREVIEW_DIR;

@Log4j2
@Service
@AllArgsConstructor
public class ReleasePreviewServiceImpl implements ReleasePreviewService {

  private static final Pattern IMAGE_EXTENSION_PATTERN = Pattern.compile(CommonConstants.IMAGE_EXTENSION);

  public ReleasePreview extract(MultipartFile file, String baseUrl) {
    try {
      ZipSafetyScanner.analyze(file);
      FileUtils.unzip(file, PREVIEW_DIR);
      return extractReadme(baseUrl, PREVIEW_DIR);
    } catch (IOException e) {
      throw new FileProcessingException(ErrorCode.FILE_PROCESSING_ERROR.getCode(),
          ErrorCode.FILE_PROCESSING_ERROR.getHelpText());
    }
  }

  public ReleasePreview extractReadme(String baseUrl, String location) throws IOException {
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
      throw new FileProcessingException(ErrorCode.FILE_PROCESSING_ERROR.getCode(), "Failed to extract README files");
    }
  }

  public String updateImagesWithDownloadUrl(String unzippedFolderPath,
      String readmeContents, String baseUrl) throws IOException {
    if (unzippedFolderPath == null) {
      throw new FileProcessingException(ErrorCode.INVALID_PATH.getCode(), "Unzipped folder path is null");
    }
    Map<String, String> imageUrls = new HashMap<>();
    try (Stream<Path> imagePathStream = Files.walk(Paths.get(unzippedFolderPath))) {
      List<Path> allImagePaths = imagePathStream
          .filter(Files::isRegularFile)
          .filter(path -> IMAGE_EXTENSION_PATTERN.matcher(
              path.getFileName().toString().toLowerCase(Locale.getDefault())).matches())
          .toList();

      allImagePaths.stream()
          .filter(Objects::nonNull)
          .forEach((Path imagePath) -> {
            var imageFileName = imagePath.getFileName().toString();
            var downloadURLFormat = String.format(IMAGE_DOWNLOAD_URL, baseUrl, imageFileName);
            imageUrls.put(imageFileName, downloadURLFormat);
          });
      return ProductContentUtils.replaceImageDirWithImageCustomId(imageUrls, readmeContents);
    }
  }

  public void processReadme(Path readmeFile, Map<String, Map<String, String>> moduleContents,
      String baseUrl, String location) throws IOException {
    try {
      var readmeContents = Files.readString(readmeFile);
      if (ProductContentUtils.hasImageDirectives(readmeContents)) {
        readmeContents = updateImagesWithDownloadUrl(location, readmeContents, baseUrl);
      }
      var readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(readmeContents);
      ProductContentUtils.mappingDescriptionSetupAndDemo(
          moduleContents,
          readmeFile.getFileName().toString(),
          readmeContentsModel
      );
    } catch (IOException e) {
      throw new FileProcessingException(ErrorCode.FILE_PROCESSING_ERROR.getCode(),
          "Failed to process README file: " + readmeFile.getFileName());
    }
  }

}
