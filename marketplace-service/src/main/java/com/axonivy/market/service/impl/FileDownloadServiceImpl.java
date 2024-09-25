package com.axonivy.market.service.impl;

import com.axonivy.market.service.FileDownloadService;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.axonivy.market.constants.CommonConstants.SLASH;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Log4j2
@Service
@Getter
public class FileDownloadServiceImpl implements FileDownloadService {

  private static final String DOC_DIR = "doc";
  private static final String ZIP_EXTENSION = ".zip";
  private static final Set<PosixFilePermission> PERMS = EnumSet.of(PosixFilePermission.OWNER_READ,
      PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.GROUP_READ,
      PosixFilePermission.GROUP_EXECUTE, PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_EXECUTE);
  private static final int THRESHOLD_ENTRIES = 10000;
  private static final int THRESHOLD_SIZE = 1000000000;
  private static final double THRESHOLD_RATIO = 10;

  private static byte[] downloadFileByRestTemplate(String url) {
    return new RestTemplate().getForObject(url, byte[].class);
  }

  private static String generateCacheStorageDirectory(String url) {
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

    // Download the file
    byte[] fileBytes = downloadFileByRestTemplate(url);
    // Save the downloaded file as a zip
    var tempZipPath = createTempFile();
    Files.write(tempZipPath, fileBytes);
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

  private static Path grantPermissionForNonUnixSystem(File tempFile) {
    var path = tempFile.toPath();
    if (tempFile.setReadable(true, true)) {
      log.warn("Cannot grant read permission to {}", path);
    }
    if (tempFile.setWritable(true, true)) {
      log.warn("Cannot grant write permission to {}", path);
    }
    if (tempFile.setExecutable(true, true)) {
      log.warn("Cannot grant exec permission to {}", path);
    }
    return path;
  }

  private void unzipFile(String zipFilePath, String location) throws IOException {
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
          break;
        }
      }
    }
  }

  private int extractFile(ZipFile zipFile, ZipEntry zipEntry, String filePath,
      int totalSizeArchive) {
    try {
      InputStream in = new BufferedInputStream(zipFile.getInputStream(zipEntry));
      BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
      byte[] bytesIn = new byte[4096];
      int totalSizeEntry = 0;
      int read;
      while ((read = in.read(bytesIn)) != -1) {
        bos.write(bytesIn, 0, read);
        totalSizeEntry += read;
        totalSizeArchive += read;

        double compressionRatio = totalSizeEntry / zipEntry.getCompressedSize();
        if (compressionRatio > THRESHOLD_RATIO) {
          break;
        }
      }
    } catch (IOException e) {
      log.error("Cannot extract file", e);
    }
    return totalSizeArchive;
  }

  private void createFolder(String location) {
    Path folderPath = Paths.get(location);
    try {
      Files.createDirectories(folderPath);
      grantNecessaryPermissionsFor(folderPath.toString());
    } catch (IOException e) {
      log.error("An error occurred while creating the folder: ", e);
    }
  }

  private void grantNecessaryPermissionsFor(String location) {
    Path folderPath = Paths.get(location);
    try {
      if (SystemUtils.IS_OS_UNIX) {
        Files.setPosixFilePermissions(folderPath, PERMS);
      } else {
        File tempFile = folderPath.toFile();
        grantPermissionForNonUnixSystem(tempFile);
      }
    } catch (IOException e) {
      log.error("An error occurred while granting permission the folder: ", e);
    }
  }
}
