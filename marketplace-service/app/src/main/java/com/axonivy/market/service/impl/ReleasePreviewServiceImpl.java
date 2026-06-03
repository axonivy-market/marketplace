package com.axonivy.market.service.impl;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.ReadmeConstants;
import com.axonivy.market.core.enums.ErrorCode;
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
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.axonivy.market.constants.PreviewConstants.PREVIEW_DIR;

@Log4j2
@Service
@AllArgsConstructor
public class ReleasePreviewServiceImpl implements ReleasePreviewService {

  private static final Pattern IMAGE_EXTENSION_PATTERN = Pattern.compile(CommonConstants.IMAGE_EXTENSION);

  @Override
  public ReleasePreview extract(MultipartFile file) {
    try {
      ZipSafetyScanner.analyze(file);
      FileUtils.unzip(file, PREVIEW_DIR);
      return extractReadme(PREVIEW_DIR);
    } catch (IOException e) {
      log.info("#extract Error extracting zip file, message: {}", e.getMessage());
      throw new FileProcessingException(ErrorCode.FILE_PROCESSING_ERROR.getCode(),
          ErrorCode.FILE_PROCESSING_ERROR.getHelpText());
    } finally {
      clearPreviewDirectory(Paths.get(PREVIEW_DIR));
    }
  }

  public ReleasePreview extractReadme(String location) throws IOException {
    Map<String, Map<String, String>> moduleContents = new HashMap<>();
    try (Stream<Path> readmePathStream = Files.walk(Paths.get(location))) {
      List<Path> readmeFiles = readmePathStream.filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().startsWith(ReadmeConstants.README_FILE_NAME))
          .toList();
      if (readmeFiles.isEmpty()) {
        return null;
      }
      for (Path readmeFile : readmeFiles) {
        processReadme(readmeFile, moduleContents, location);
      }
      return ReleasePreview.from(moduleContents);
    }
  }

  public String updateImagesWithInlineImages(String unzippedFolderPath, String readmeContents) throws IOException {
    Map<String, String> imageDataUris = new HashMap<>();
    try (Stream<Path> imagePathStream = Files.walk(Paths.get(unzippedFolderPath))) {
      List<Path> allImagePaths = imagePathStream
          .filter(Files::isRegularFile)
          .filter(path -> IMAGE_EXTENSION_PATTERN.matcher(
              path.getFileName().toString().toLowerCase(Locale.getDefault())).matches())
          .toList();

      for (Path imagePath : allImagePaths.stream().filter(Objects::nonNull).toList()) {
        var imageFileName = imagePath.getFileName().toString();
        imageDataUris.put(imageFileName, buildInlineImageSource(imagePath));
      }
      return ProductContentUtils.replaceImageDirWithImageCustomId(imageDataUris, readmeContents);
    }
  }

  private String buildInlineImageSource(Path imagePath) throws IOException {
    var mediaType = resolveInlineMediaType(imagePath);
    var base64Content = Base64.getEncoder().encodeToString(Files.readAllBytes(imagePath));
    return String.format("data:%s;base64,%s", mediaType, base64Content);
  }

  private String resolveInlineMediaType(Path imagePath) throws IOException {
    return Optional.ofNullable(Files.probeContentType(imagePath)).orElse("application/octet-stream");
  }

  public void processReadme(Path readmeFile, Map<String, Map<String, String>> moduleContents,
      String location) throws IOException {
    var readmeContents = Files.readString(readmeFile);
    if (ProductContentUtils.hasImageDirectives(readmeContents)) {
      readmeContents = updateImagesWithInlineImages(location, readmeContents);
    }
    var readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(readmeContents);
    ProductContentUtils.mappingDescriptionSetupAndDemoAndComponent(
        moduleContents,
        readmeFile.getFileName().toString(),
        readmeContentsModel
    );
  }

  private void clearPreviewDirectory(Path unzipDir) {
    try {
      FileUtils.clearDirectory(unzipDir);
      Files.deleteIfExists(unzipDir);
    } catch (IOException e) {
      log.warn("#extract Failed to clear preview directory {}: {}", PREVIEW_DIR, e.getMessage());
    }
  }
}
