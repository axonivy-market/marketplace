package com.axonivy.market.service.impl;

import com.axonivy.market.service.FileDownloadService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.stereotype.Service;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.axonivy.market.constants.CommonConstants.SLASH;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Log4j2
@Service
public class FileDownloadServiceImpl implements FileDownloadService {

  private static final String DOC_DIR = "doc";
  private static final String ZIP_EXTENSION = ".zip";
  private static final Set<PosixFilePermission> PERMS = EnumSet.allOf(PosixFilePermission.class);
  private static final int THRESHOLD_ENTRIES = 10000;
  private static final int THRESHOLD_SIZE = 1000000000;
  private static final double THRESHOLD_RATIO = 10;

  private byte[] downloadFileByRestTemplate(String url) {
    return new RestTemplate().getForObject(url, byte[].class);
  }

  private String generateCacheStorageDirectory(String url) {
    url = url.substring(0, url.lastIndexOf(SLASH));
    var urlArrays = Arrays.asList(url.split(SLASH));
    Collections.reverse(urlArrays);
    var urlIterator = urlArrays.iterator();

    // Only get 3 last paths of url. e.g: artifactGroup/artifact/version
    int index = 0;
    List<String> paths = new ArrayList<>();
    do {
      if (urlIterator.hasNext()) {
        paths.add(urlIterator.next());
      }
      index++;
    } while (index < 3);
    Collections.reverse(paths);
    return ROOT_STORAGE + File.separator + String.join(File.separator, paths) + File.separator + DOC_DIR;
  }

  @Override
  public String downloadAndUnzipFile(String url, boolean isForce) throws IOException {
    if (StringUtils.isBlank(url) || !url.endsWith(ZIP_EXTENSION)) {
      log.warn("Request URL not a ZIP file - {}", url);
      return EMPTY;
    }

    String location = generateCacheStorageDirectory(url);
    File cacheFolder = new File(location);
    if (cacheFolder.exists() && cacheFolder.isDirectory() && !isForce) {
      log.warn("Data is already in {}", location);
      return EMPTY;
    } else {
      createFolder(location);
    }

    // Download the file and then save to downloaded file as a zip
    Path tempZipPath = createTempFile();
    Files.write(tempZipPath, downloadFileByRestTemplate(url));
    // Unzip the file
    unzipFile(tempZipPath.toString(), location);
    // Grant again access control on doc folder
    grantNecessaryPermissionsFor(location);
    // Clean up the temporary zip file
    Files.delete(tempZipPath);
    return location;
  }

  private Path createTempFile() throws IOException {
    Path tempZipPath;
    var tempFileName = UUID.randomUUID().toString();
    if (SystemUtils.IS_OS_UNIX) {
      FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(PERMS);
      tempZipPath = Files.createTempFile(tempFileName, ZIP_EXTENSION, attr);
    } else {
      File tempFile = Files.createTempFile(tempFileName, ZIP_EXTENSION).toFile();
      tempZipPath = grantPermissionForNonUnixSystem(tempFile);
    }
    return tempZipPath;
  }

  private Path grantPermissionForNonUnixSystem(File tempFile) {
    var path = tempFile.toPath();
    if (tempFile.setReadable(true, false)) {
      log.warn("Cannot grant read permission to {}", path);
    }
    if (tempFile.setWritable(true, false)) {
      log.warn("Cannot grant write permission to {}", path);
    }
    if (tempFile.setExecutable(true, false)) {
      log.warn("Cannot grant exec permission to {}", path);
    }
    return path;
  }

  public void unzipFile(String zipFilePath, String location) throws IOException {
    Path destDirPath = Paths.get(location).toAbsolutePath().normalize();
    try (ZipFile zipFile = new ZipFile(new File(zipFilePath))) {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      int totalSizeArchive = 0;
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

  private void createFolder(String location) {
    Path folderPath = Paths.get(location);
    try {
      Files.createDirectories(folderPath);
    } catch (IOException e) {
      log.error("An error occurred while creating the folder: ", e);
    }
  }

  public void grantNecessaryPermissionsFor(String location) {
    Path folderPath = Paths.get(location);
    try {
      if (SystemUtils.IS_OS_UNIX) {
        log.warn("UNIX_OS detected: grant permission for {}", location);
        Files.setPosixFilePermissions(folderPath, PERMS);
      } else {
        log.warn("NON_UNIX_OS detected: grant permission for {}", location);
        File tempFile = folderPath.toFile();
        grantPermissionForNonUnixSystem(tempFile);
      }
    } catch (IOException e) {
      log.error("An error occurred while granting permission the folder: ", e);
    }
  }
}
