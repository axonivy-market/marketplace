package com.axonivy.market.service.impl;

import com.axonivy.market.entity.Metadata;
import com.axonivy.market.service.FileDownloadService;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
@Log4j2
public class FileDownloadServiceImpl implements FileDownloadService {
  private static final String ZIP_EXTENSION = ".zip";
  private static final Set<PosixFilePermission> PERMS = EnumSet.allOf(PosixFilePermission.class);
  private static final int THRESHOLD_ENTRIES = 10000;
  private static final int THRESHOLD_SIZE = 1000000000;
  private static final double THRESHOLD_RATIO = 10;

  private byte[] downloadFileByRestTemplate(String url) {
    return new RestTemplate().getForObject(url, byte[].class);
  }

  @Override
  public String downloadAndUnzipProductContentFile(String url, Metadata snapShotMetadata) throws IOException {
    String unzippedFilePath = String.join(File.separator, ROOT_STORAGE_FOR_PRODUCT_CONTENT,
        snapShotMetadata.getArtifactId());
    createFolder(unzippedFilePath);

    Path tempZipPath = createTempFile();
    Files.write(tempZipPath, downloadFileByRestTemplate(url));

    unzipFile(tempZipPath.toString(), unzippedFilePath);

    Files.delete(tempZipPath);
    return unzippedFilePath;
  }

  private Path createTempFile() throws IOException {
    Path tempZipPath;
    var tempFileName = UUID.randomUUID().toString();
    if (SystemUtils.IS_OS_UNIX) {
      FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(PERMS);
      tempZipPath = Files.createTempFile(tempFileName, ZIP_EXTENSION, attr);
    } else {
      File tempFile = Files.createTempFile(tempFileName, ZIP_EXTENSION).toFile();
      tempZipPath = tempFile.toPath();
    }
    return tempZipPath;
  }

  public int unzipFile(String zipFilePath, String location) throws IOException {
    int totalSizeArchive = 0;
    Path destDirPath = Paths.get(location).toAbsolutePath().normalize();
    try (ZipFile zipFile = new ZipFile(new File(zipFilePath))) {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      int totalEntryArchive = 0;

      while (entries.hasMoreElements()) {
        ZipEntry zipEntry = entries.nextElement();
        Path entryPath = destDirPath.resolve(zipEntry.getName()).normalize();
        if (!entryPath.startsWith(destDirPath)) {
          throw new IOException("Bad zip zipEntry: " + zipEntry.getName());
        }
        if (zipEntry.isDirectory()) {
          createFolder(entryPath.toString());
        } else {
          totalEntryArchive++;
          totalSizeArchive = extractFile(zipFile, zipEntry, entryPath.toString(), totalSizeArchive);
        }
        if (totalSizeArchive > THRESHOLD_SIZE || totalEntryArchive > THRESHOLD_ENTRIES) {
          log.warn("Unzip is skipped due to threshold issue {} {}", totalSizeArchive, totalEntryArchive);
          break;
        }
      }
    }
    return totalSizeArchive;
  }

  public int extractFile(ZipFile zipFile, ZipEntry zipEntry, String filePath,
      int totalSizeArchive) {
    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
      InputStream stream = new BufferedInputStream(zipFile.getInputStream(zipEntry));
      byte[] bytesIn = new byte[4096];
      int totalSizeEntry = 0;
      int read;
      while ((read = stream.read(bytesIn)) != -1) {
        bos.write(bytesIn, 0, read);
        totalSizeEntry += read;
        totalSizeArchive += read;

        var compressionRatio = totalSizeEntry / zipEntry.getCompressedSize();
        if (compressionRatio > THRESHOLD_RATIO) {
          log.warn("Extract file is skipped due to threshold issue {}", compressionRatio);
          break;
        }
      }
      stream.close();
    } catch (IOException e) {
      log.error("Cannot extract file", e);
    }
    return totalSizeArchive;
  }

  public Path createFolder(String location) {
    Path folderPath = Paths.get(location);
    try {
      Files.createDirectories(folderPath);
    } catch (IOException e) {
      log.error("An error occurred while creating the folder: ", e);
    }
    return folderPath;
  }

  @Override
  public void deleteDirectory(Path path) {
    try (Stream<Path> paths = Files.walk(path)) {
      paths.sorted(Comparator.reverseOrder())
          .forEach(p -> {
            try {
              Files.delete(p);
            } catch (IOException e) {
              log.error("Failed to delete files in {} - {}", p, e.getMessage());
            }
          });
    } catch (IOException e) {
      log.error("Failed to walk directory {} - {}", path, e.getMessage());
    }
  }
}
