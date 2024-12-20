package com.axonivy.market.service.impl;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.ReadmeConstants;
import com.axonivy.market.model.ReadmeContentsModel;
import com.axonivy.market.model.ReleasePreview;
import com.axonivy.market.service.ReleasePreviewService;
import com.axonivy.market.util.ProductContentUtils;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.axonivy.market.constants.DirectoryConstants.PREVIEW_DIR;

@Log4j2
@Service
@AllArgsConstructor
public class ReleasePreviewServiceImpl implements ReleasePreviewService {

  private final static String IMAGE_DOWNLOAD_URL = "%s/api/image/preview/%s";

  @Override
  public ReleasePreview extract(MultipartFile file, String baseUrl) {
    unzip(file);
    return extractREADME(baseUrl);
  }

  private void unzip(MultipartFile file) {
    try {
      File extractDir = new File(PREVIEW_DIR);
      prepareUnZipDirectory(extractDir.toPath());

      try (ZipInputStream zipInputStream = new ZipInputStream(file.getInputStream())) {
        ZipEntry entry;
        while ((entry = zipInputStream.getNextEntry()) != null) {
          File outFile = new File(extractDir, entry.getName());
          if (entry.isDirectory()) {
            outFile.mkdirs();
          } else {
            new File(outFile.getParent()).mkdirs();
            try (FileOutputStream fos = new FileOutputStream(outFile)) {
              byte[] buffer = new byte[1024];
              int length;
              while ((length = zipInputStream.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
              }
            }
          }
          zipInputStream.closeEntry();
        }
      }
    } catch (IOException e) {
      log.error("#unzip An exception occurred when unzip file {} - message {}", file.getName(), e.getMessage());
    }
  }

  private ReleasePreview extractREADME(String baseUrl) {
    Map<String, Map<String, String>> moduleContents = new HashMap<>();
    try (Stream<Path> readmePathStream = Files.walk(Paths.get(PREVIEW_DIR))) {
      List<Path> readmeFiles = readmePathStream.filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().startsWith(ReadmeConstants.README_FILE_NAME))
          .toList();
      if (readmeFiles.isEmpty()) {
        return null;
      }
      for (Path readmeFile : readmeFiles) {
        processReadme(readmeFile, moduleContents, baseUrl);
      }
      return ReleasePreview.from(moduleContents);
    } catch (IOException e) {
      log.error("Cannot get README file's content from folder {}: {}",
          com.axonivy.market.constants.DirectoryConstants.PREVIEW_DIR, e.getMessage());
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

  private static void prepareUnZipDirectory(Path directory) {
    try {
      if (Files.exists(directory)) {
        Files.walk(directory)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
      }
      Files.createDirectories(directory);
    } catch (IOException e) {
      log.error("#prepareDirectory: Error managing directory {} : {}", directory.toString(), e.getMessage());
    }
  }

  private void processReadme(Path readmeFile, Map<String, Map<String, String>> moduleContents,
      String baseUrl) throws IOException {
    String readmeContents = Files.readString(readmeFile);
    if (ProductContentUtils.hasImageDirectives(readmeContents)) {
      readmeContents = updateImagesWithDownloadUrl(PREVIEW_DIR, readmeContents, baseUrl);
    }
    ReadmeContentsModel readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(readmeContents);
    ProductContentUtils.mappingDescriptionSetupAndDemo(
        moduleContents,
        readmeFile.getFileName().toString(),
        readmeContentsModel
    );
  }
}
