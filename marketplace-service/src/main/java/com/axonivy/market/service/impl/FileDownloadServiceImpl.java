package com.axonivy.market.service.impl;

import com.axonivy.market.bo.DownloadOption;
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.util.FileUtils;
import com.axonivy.market.util.validator.AuthorizationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
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
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Service
@Log4j2
@RequiredArgsConstructor
public class FileDownloadServiceImpl implements FileDownloadService {
  private static final String DOC_DIR = "doc";
  private static final String ZIP_EXTENSION = ".zip";
  private static final Set<PosixFilePermission> PERMS = EnumSet.allOf(PosixFilePermission.class);
  private static final int THRESHOLD_SIZE = 1000000000;
  public static final String IAR = "iar";

  private final AuthorizationUtils authorizationUtils;

  @Override
  public byte[] downloadFile(String url) {
    return new RestTemplate().getForObject(url, byte[].class);
  }

  public byte[] safeDownload(String url) {
    try {
      String trustedUrl = authorizationUtils.resolveTrustedUrl(url);
      return downloadFile(trustedUrl);
    }catch (IllegalArgumentException e) {
      log.warn("Unsafe or disallowed URL provided: {}", url, e);
    } catch (HttpClientErrorException e) {
      log.warn("Fail to download at URL: {}", url);
    }
    return EMPTY.getBytes();
  }

  @Override
  public String downloadAndUnzipFile(String url, DownloadOption downloadOption) throws IOException {
    if (StringUtils.isBlank(url) || !StringUtils.endsWithAny(url, ZIP_EXTENSION, IAR)) {
      log.warn("Request URL not a ZIP/iar file - {}", url);
      return EMPTY;
    }

    String location = determineLocation(url, downloadOption);
    Path tempZipPath = createTempFileFromUrlAndExtractToLocation(url, location, downloadOption);
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

    Path tempZipPath = createTempFile();
    byte[] fileContent = safeDownload(url);
    if (fileContent == null || fileContent.length == 0) {
      log.warn("Downloaded file is empty or null from URL: {}", url);
      return null;
    }
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

      while (entries.hasMoreElements()) {
        ZipEntry zipEntry = entries.nextElement();
        Path entryPath = destDirPath.resolve(zipEntry.getName()).normalize();
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
    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
      InputStream stream = new BufferedInputStream(zipFile.getInputStream(zipEntry));
      byte[] bytesIn = new byte[4096];
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
    Path folderPath = Paths.get(location);
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
      paths.sorted(Comparator.reverseOrder()).forEach(p -> {
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
    Path folderPath = Paths.get(location);
    try {
      if (SystemUtils.IS_OS_UNIX) {
        log.warn("UNIX_OS detected: grant permission for {}", location);
        Files.setPosixFilePermissions(folderPath, PERMS);
      } else {
        log.warn("NON_UNIX_OS detected: grant permission for {}", location);
        folderPath = grantPermissionForNonUnixSystem(folderPath.toFile());
      }
    } catch (IOException e) {
      log.error("An error occurred while granting permission the folder: ", e);
    }
    return folderPath;
  }

  private Path grantPermissionForNonUnixSystem(File tempFile) {
    if (tempFile.setReadable(true, false)) {
      log.warn("Cannot grant read permission to {}", tempFile.toPath());
    }
    if (tempFile.setWritable(true, false)) {
      log.warn("Cannot grant write permission to {}", tempFile.toPath());
    }
    if (tempFile.setExecutable(true, false)) {
      log.warn("Cannot grant exec permission to {}", tempFile.toPath());
    }
    return tempFile.toPath();
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
    return ROOT_STORAGE_FOR_CACHE + File.separator + String.join(File.separator, paths) + File.separator + DOC_DIR;
  }
}