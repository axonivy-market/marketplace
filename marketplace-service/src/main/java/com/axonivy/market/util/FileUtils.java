package com.axonivy.market.util;

import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.exceptions.model.FileProcessingException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils {
  public static final int DEFAULT_BUFFER_SIZE = 8192;
  public static final int DEFAULT_BUFFER_GITHUB_SIZE = 4096;
  private static final String DEPLOY_YAML_FILE_NAME = "deploy.options.yaml";
  private static final String ENTRY_OUTSIDE_TARGET_DIR = "entry is outside the target dir";

  public static File createFile(String fileName) throws IOException {
    var file = new File(fileName);
    var parentDir = file.getParentFile();
    createDirectoryFromFile(parentDir);
    if (!file.exists() && !file.createNewFile()) {
      throw new IOException("Failed to create file: " + file.getAbsolutePath());
    }
    return file;
  }

  public static void writeToFile(File file, String content) throws IOException {
    try (var writer = new FileWriter(file, false)) {
      writer.write(content);
    }
  }

  public static void unzipArtifact(InputStream inputStream, File unzipDir) {
    try (var zis = new ZipInputStream(inputStream)) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        processZipEntry(zis, entry, unzipDir);
        zis.closeEntry();
      }
    } catch (IOException e) {
      log.error("Error unzipping artifact: {}", e.getMessage());
    }
  }

  private static void processZipEntry(ZipInputStream zis, ZipEntry entry, File unzipDir) throws IOException {
    var entryPath = Paths.get(entry.getName()).normalize();
    var resolvedPath = unzipDir.toPath().resolve(entryPath).normalize();
    if (!resolvedPath.startsWith(unzipDir.toPath())) {
      throw new IllegalStateException(ENTRY_OUTSIDE_TARGET_DIR + entry.getName());
    }

    var outFile = resolvedPath.toFile();
    if (entry.isDirectory()) {
      outFile.mkdirs();
    } else {
      createParentDirectories(outFile);
      writeFileFromZip(zis, outFile);
    }
  }

  public static void createParentDirectories(File outFile) {
    var parentDir = outFile.getParentFile();
    if (parentDir != null && !parentDir.exists()) {
      boolean created = parentDir.mkdirs();
      if (!created && !parentDir.exists()) {
        throw new IllegalStateException("Failed to create parent directories: " + outFile.getAbsolutePath());
      }
    }
  }

  public static void writeFileFromZip(InputStream zis, File outFile) throws IOException {
    try (var fos = new FileOutputStream(outFile)) {
      var buffer = new byte[DEFAULT_BUFFER_GITHUB_SIZE];
      int length;
      while ((length = zis.read(buffer)) > 0) {
        fos.write(buffer, 0, length);
      }
    }
  }

  // Common method to extract .zip file
  public static void unzip(InputStreamSource file, String location) throws IOException {
    var extractDir = new File(location);
    prepareUnZipDirectory(extractDir.toPath());

    try {
      unzipArtifact(file.getInputStream(), extractDir);
    } catch (IOException | IllegalStateException e) {
      throw new IOException("Error unzipping file", e);
    }
  }

  private static void createDirectoryFromFile(File file) throws IOException {
    if (file != null && !file.mkdirs() && !file.isDirectory()) {
      throw new IOException("Failed to create directory: " + file);
    }
  }

  public static void prepareUnZipDirectory(Path directory) throws IOException {
    clearDirectory(directory);
    Files.createDirectories(directory);
  }

  public static void clearDirectory(Path path) throws IOException {
    if (Files.exists(path)) {
      try (Stream<Path> paths = Files.walk(path)) {
        paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
      }
    }
  }

  public static File createNewFile(String location) {
    return new File(location);
  }

  public static void writeBlobAsChunks(InputStream in, OutputStream out) throws IOException {
    var buffer = new byte[DEFAULT_BUFFER_SIZE];
    int bytesRead;
    while ((bytesRead = in.read(buffer)) != -1) {
      out.write(buffer, 0, bytesRead);
    }
  }

  public static OutputStream buildArtifactStreamFromArtifactUrls(Iterable<String> urls, OutputStream outputStream) {
    try (var zipOut = new ZipOutputStream(outputStream)) {
      for (String fileUrl : urls) {
        ResponseEntity<Resource> resourceResponse = HttpFetchingUtils.fetchResourceUrl(fileUrl);
        if (null == resourceResponse || !resourceResponse.getStatusCode().is2xxSuccessful() ||
            resourceResponse.getBody() == null) {
          continue;
        }
        String fileName = HttpFetchingUtils.extractFileNameFromUrl(fileUrl);
        try (var fileInputStream = resourceResponse.getBody().getInputStream()) {
          addNewFileToZip(fileName, zipOut, fileInputStream);
        }
      }
      zipConfigurationOptions(zipOut);
      zipOut.finish();
    } catch (IOException e) {
      log.error("Cannot create ZIP file {}", e.getMessage());
      return null;
    }
    return outputStream;
  }

  private static void zipConfigurationOptions(ZipOutputStream zipOut) throws IOException {
    final String configFile = DEPLOY_YAML_FILE_NAME;
    var resource = new ClassPathResource("app-zip/" + configFile);
    try (var in = resource.getInputStream()) {
      addNewFileToZip(configFile, zipOut, in);
    }
  }

  private static void addNewFileToZip(String fileName, ZipOutputStream zipOut, InputStream in) throws IOException {
    var entry = new ZipEntry(fileName);
    zipOut.putNextEntry(entry);
    try {
      writeBlobAsChunks(in, zipOut);
    } finally {
      zipOut.closeEntry();
    }
  }
}
