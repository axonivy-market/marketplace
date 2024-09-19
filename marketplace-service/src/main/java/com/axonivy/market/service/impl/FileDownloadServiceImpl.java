package com.axonivy.market.service.impl;

import com.axonivy.market.service.FileDownloadService;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.axonivy.market.constants.CommonConstants.SLASH;

@Log4j2
@Service
@Getter
public class FileDownloadServiceImpl implements FileDownloadService {

  private static final String ROOT_DIR = "user.dir";
  private static final String CACHED_DIR = "cached";
  private static final String DOC_DIR = "doc";
  private static final String ZIP_EXTENSION = ".zip";
  private static final String TEMP_FILE_NAME = "downloaded";

  public static void main(String[] args) throws Exception {
    FileDownloadService server = new FileDownloadServiceImpl();
    server.downloadAndUnzipFile("https://nexus.axonivy.com/repository/maven-releases/com/axonivy/portal/portal-guide/11.4.0-m263/portal-guide-11.4.0-m263.zip", true);
  }

  private static byte[] downloadFileByRestTemplate(String url) {
    return new RestTemplate().getForObject(url, byte[].class);
  }

  private static String generateCacheStorageDirectory(String url) {
    String destinationDirectory = System.getProperty(ROOT_DIR) + File.separator + CACHED_DIR;
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
    return destinationDirectory + File.separator + String.join(File.separator, paths) + File.separator + DOC_DIR;
  }

  @Override
  public void downloadAndUnzipFile(String url, boolean isForce) throws Exception {
    if (StringUtils.isBlank(url) || !url.endsWith(ZIP_EXTENSION)) {
      log.warn("Request URL not a ZIP file - {}", url);
      return;
    }

    String location = generateCacheStorageDirectory(url);
    File cacheFolder = new File(location);
    if (cacheFolder.exists() && cacheFolder.isDirectory() && !isForce) {
      log.warn("Data is already - {}", location);
      return;
    } else {
      cacheFolder.mkdirs();
    }

    // Download the file
    byte[] fileBytes = downloadFileByRestTemplate(url);

    // Save the downloaded file as a zip
    Path tempZipPath = Files.createTempFile(TEMP_FILE_NAME, ZIP_EXTENSION);
    Files.write(tempZipPath, fileBytes);

    // Unzip the file
    unzipFile(tempZipPath.toString(), location);

    // Clean up the temporary zip file
    Files.delete(tempZipPath);
  }

  private void unzipFile(String zipFilePath, String location) throws IOException {
    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
      ZipEntry entry = zis.getNextEntry();
      while (entry != null) {
        String filePath = location + File.separator + entry.getName();
        if (entry.isDirectory()) {
          new File(filePath).mkdirs();
        } else {
          extractFile(zis, filePath);
        }
        zis.closeEntry();
        entry = zis.getNextEntry();
      }
    }
  }

  private void extractFile(ZipInputStream zis, String filePath) throws IOException {
    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
      byte[] bytesIn = new byte[4096];
      int read;
      while ((read = zis.read(bytesIn)) != -1) {
        bos.write(bytesIn, 0, read);
      }
    }
  }
}
