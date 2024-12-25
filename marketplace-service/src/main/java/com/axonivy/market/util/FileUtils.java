package com.axonivy.market.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils {

  public static File createFile(String fileName) throws IOException {
    File file = new File(fileName);
    File parentDir = file.getParentFile();
    createDirectoryFromFile(parentDir);
    if (!file.exists() && !file.createNewFile()) {
      throw new IOException("Failed to create file: " + file.getAbsolutePath());
    }
    return file;
  }

  public static void writeToFile(File file, String content) throws IOException {
    try (FileWriter writer = new FileWriter(file, false)) {
      writer.write(content);
    }
  }

  public static void unzip(MultipartFile file, String location) throws IOException {
    File extractDir = new File(location);
    prepareUnZipDirectory(extractDir.toPath());

    try (ZipInputStream zipInputStream = new ZipInputStream(file.getInputStream())) {
      ZipEntry entry;
      while ((entry = zipInputStream.getNextEntry()) != null) {
        Path entryPath = Paths.get(entry.getName()).normalize();
        Path resolvedPath = extractDir.toPath().resolve(entryPath).normalize();

        if (!resolvedPath.startsWith(extractDir.toPath())) {
          throw new IOException("Entry is outside the target dir: " + entry.getName());
        }

        File outFile = resolvedPath.toFile();
        if (entry.isDirectory()) {
          createDirectoryFromFile(outFile);
        } else {
          File parentDir = outFile.getParentFile();
          createDirectoryFromFile(parentDir);
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
    } catch (IOException e) {
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
        paths.sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
      }
    }
  }

  public static File createNewFile(String location) {
    return new File(location);
  }
}
