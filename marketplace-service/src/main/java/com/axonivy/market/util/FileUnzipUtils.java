package com.axonivy.market.util;

import lombok.extern.log4j.Log4j2;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Log4j2
public class FileUnzipUtils {
  private FileUnzipUtils() {}

  public static void downloadFile(String fileURL, String saveDir) throws IOException {
    URL url = new URL(fileURL);

    try (InputStream in = url.openStream()) {
      Path path = Paths.get(saveDir);
      Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  public static void unzipFile(String zipFilePath, String destDir) throws IOException {
    byte[] buffer = new byte[1024];

    Path unzipPath = Paths.get(destDir);
    if (!Files.exists(unzipPath)) {
      Files.createDirectories(unzipPath);
    }

    try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(Paths.get(zipFilePath)))) {
      ZipEntry zipEntry = zis.getNextEntry();
      while (zipEntry != null) {
        Path newPath = unzipPath.resolve(zipEntry.getName()).normalize();

        if (!newPath.startsWith(unzipPath)) {
          throw new IOException("Bad zip entry: " + zipEntry.getName());
        }

        if (zipEntry.isDirectory()) {
          Files.createDirectories(newPath);
        } else {
          Files.createDirectories(newPath.getParent());
          try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(newPath))) {
            int len;
            while ((len = zis.read(buffer)) > 0) {
              bos.write(buffer, 0, len);
            }
          }
        }
        zipEntry = zis.getNextEntry();
      }
      zis.closeEntry();
    } catch (IOException e) {
      log.error("Error while unzipping: {}", e.getMessage());
    }
  }
}
