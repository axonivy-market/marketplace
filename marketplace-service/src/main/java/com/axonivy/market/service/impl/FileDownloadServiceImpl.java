package com.axonivy.market.service.impl;

import com.axonivy.market.bo.DownloadOption;
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
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
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.axonivy.market.constants.CommonConstants.SLASH;
import static com.axonivy.market.constants.CommonConstants.ZIP_EXTENSION;
import static com.axonivy.market.constants.DirectoryConstants.DOC_DIR;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Service
@Log4j2
@RequiredArgsConstructor
public class FileDownloadServiceImpl implements FileDownloadService {
  private static final Set<PosixFilePermission> PERMS = EnumSet.allOf(PosixFilePermission.class);
  private static final int THRESHOLD_SIZE = 1_000_000_000;
  private static final String IAR = "iar";
  private static final int URL_PATHS_TO_GET = 3;
  private static final int BUFFER_SIZE = 4096;
  private final RestTemplate restTemplate;

  @Override
  public byte[] downloadFile(String url) {
    try {
      return restTemplate.getForObject(url, byte[].class);
    } catch (RestClientException e) {
      log.warn("#downloadFile Failed to fetch resource from URL: {}", url, e);
      return new byte[0];
    }
  }

  @Override
  public String getFileAsString(String url) {
    try {
      return restTemplate.getForObject(url, String.class);
    } catch (RestClientException e) {
      log.warn("Failed to fetch resource from URL: {}", url, e);
      return EMPTY;
    }

  }


  @Override
  public ResponseEntity<Resource> fetchUrlResource(String url) {
      try {
        return restTemplate.exchange(url, HttpMethod.GET, null, Resource.class);
      } catch (RestClientException e) {
        log.warn("Failed to fetch resource from URL: {}", url, e);
    }
    return null;
  }

  @Override
  public String downloadAndUnzipFile(String url, DownloadOption downloadOption) throws IOException {
    if (StringUtils.isBlank(url) || !StringUtils.endsWithAny(url, ZIP_EXTENSION, IAR)) {
      log.warn("Request URL not a ZIP/iar file - {}", url);
      return EMPTY;
    }
    String location = determineLocation(url, downloadOption);
    var tempZipPath = createTempFileFromUrlAndExtractToLocation(url, location, downloadOption);
    if (tempZipPath != null) {
      if (downloadOption != null && downloadOption.isShouldGrantPermission()) {
        grantNecessaryPermissionsFor(location);
      }
      Files.delete(tempZipPath);
    }
    return location;
  }

  private String determineLocation(String url, DownloadOption downloadOption) {
    String location;
    if (downloadOption != null && StringUtils.isNotBlank(downloadOption.getWorkingDirectory())) {
      location = downloadOption.getWorkingDirectory();
    } else {
      location = generateCacheStorageDirectory(url);
    }
    return location;
  }

  private Path createTempFileFromUrlAndExtractToLocation(String url, String location,
      DownloadOption downloadOption) throws IOException {
    if (prepareDirectoryForUnzipProcess(location, downloadOption)) {
      return null;
    }

    byte[] fileContent = downloadFile(url);
    if (fileContent == null || fileContent.length == 0) {
      log.warn("Cannot download file or file is empty from url: {}", url);
      return null;
    }

    var tempZipPath = createTempFile();
    Files.write(tempZipPath, fileContent);
    unzipFile(tempZipPath.toString(), location);
    return tempZipPath;
  }

  private boolean prepareDirectoryForUnzipProcess(String location, DownloadOption downloadOption) {
    var isDataExistedInFolder = false;
    var cacheFolder = FileUtils.createNewFile(location);
    boolean isForced = Optional.ofNullable(downloadOption).map(DownloadOption::isForced).orElse(false);
    if (cacheFolder.exists() && cacheFolder.isDirectory()) {
      if (isForced) {
        log.warn("Forced delete {} for re-create again due to DownloadOption::isForced is true", location);
        deleteDirectory(cacheFolder.toPath());
      } else if (ObjectUtils.isNotEmpty(cacheFolder.listFiles())) {
        isDataExistedInFolder = true;
      }
    }
    if (!isDataExistedInFolder) {
      createFolder(location);
    }
    return isDataExistedInFolder;
  }

  private static Path createTempFile() throws IOException {
    Path tempZipPath;
    var tempFileName = UUID.randomUUID().toString();
    if (SystemUtils.IS_OS_UNIX) {
      FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(PERMS);
      tempZipPath = Files.createTempFile(tempFileName, ZIP_EXTENSION, attr);
    } else {
      var tempFile = Files.createTempFile(tempFileName, ZIP_EXTENSION).toFile();
      tempZipPath = tempFile.toPath();
    }
    return tempZipPath;
  }

  public int unzipFile(String zipFilePath, String location) throws IOException {
    var totalSizeArchive = 0;
    var destDirPath = Paths.get(location).toAbsolutePath().normalize();
    try (var zipFile = new ZipFile(new File(zipFilePath))) {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();

      while (entries.hasMoreElements()) {
        ZipEntry zipEntry = entries.nextElement();
        var entryPath = destDirPath.resolve(zipEntry.getName()).normalize();
        if (!entryPath.startsWith(destDirPath)) {
          throw new IOException("Bad zip zipEntry: " + zipEntry.getName());
        }
        if (zipEntry.isDirectory()) {
          createFolder(entryPath.toString());
        } else {
          totalSizeArchive = extractFile(zipFile, zipEntry, entryPath.toString(), totalSizeArchive);
        }
        if (totalSizeArchive > THRESHOLD_SIZE) {
          log.warn("Unzip is skipped due to threshold issue {}", totalSizeArchive);
          break;
        }
      }
    }
    return totalSizeArchive;
  }

  public int extractFile(ZipFile zipFile, ZipEntry zipEntry, String filePath, int totalSizeArchive) {
    try (var bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
      InputStream stream = new BufferedInputStream(zipFile.getInputStream(zipEntry));
      var bytesIn = new byte[BUFFER_SIZE];
      int read;
      while ((read = stream.read(bytesIn)) != -1) {
        bos.write(bytesIn, 0, read);
        totalSizeArchive += read;
      }
      stream.close();
    } catch (IOException e) {
      log.error("Cannot extract file", e);
    }
    return totalSizeArchive;
  }

  public Path createFolder(String location) {
    var folderPath = Paths.get(location);
    try {
      Files.createDirectories(folderPath);
    } catch (IOException e) {
      log.error("An error occurred while creating the folder: {}", e.getMessage());
    }
    return folderPath;
  }

  @Override
  public void deleteDirectory(Path path) {
    try (Stream<Path> paths = Files.walk(path)) {
      paths.sorted(Comparator.reverseOrder()).forEach((Path p) -> {
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

  public Path grantNecessaryPermissionsFor(String location) {
    var folderPath = Paths.get(location);
    try {
      if (SystemUtils.IS_OS_UNIX) {
        log.warn("UNIX_OS detected: grant permission for {}", location);
        Files.setPosixFilePermissions(folderPath, PERMS);
      }
    } catch (IOException e) {
      log.error("An error occurred while granting permission the folder: ", e);
    }
    return folderPath;
  }

  public String generateCacheStorageDirectory(String url) {
    url = url.substring(0, url.lastIndexOf(SLASH));
    var urlArrays = Arrays.asList(url.split(SLASH));
    Collections.reverse(urlArrays);
    var urlIterator = urlArrays.iterator();

    // Only get 3 last paths of url. e.g: artifactGroup/artifact/version
    var index = 0;
    List<String> paths = new ArrayList<>();
    do {
      if (urlIterator.hasNext()) {
        paths.add(urlIterator.next());
      }
      index++;
    } while (index < URL_PATHS_TO_GET);
    Collections.reverse(paths);
    return ROOT_STORAGE_FOR_CACHE + File.separator + String.join(File.separator, paths) + File.separator + DOC_DIR;
  }
}
