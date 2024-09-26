package com.axonivy.market.util;

import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.entity.Metadata;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Log4j2
public class MetadataReaderUtils {

  private MetadataReaderUtils() {
  }

  public static void parseMetadataFromString(String xmlData, Metadata metadata) {
    try {
      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = builder.parse(new InputSource(new StringReader(xmlData)));
      document.getDocumentElement().normalize();
      DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(MavenConstants.DATE_TIME_FORMAT);
      LocalDateTime lastUpdated = LocalDateTime.parse(
          Objects.requireNonNull(getElementValue(document, MavenConstants.LAST_UPDATED_TAG)),
          dateTimeFormatter);
      if (lastUpdated.equals(metadata.getLastUpdated())) {
        return;
      }
      metadata.setLastUpdated(lastUpdated);
      metadata.setLatest(getElementValue(document, MavenConstants.LATEST_VERSION_TAG));
      metadata.setRelease(getElementValue(document, MavenConstants.LATEST_RELEASE_TAG));
      NodeList versionNodes = document.getElementsByTagName(MavenConstants.VERSION_TAG);
      for (int i = 0; i < versionNodes.getLength(); i++) {
        metadata.getVersions().add(versionNodes.item(i).getTextContent());
      }
    } catch (Exception e) {
      log.error("Metadata Reader: can not read the metadata of {}", xmlData);
    }
  }

  public static void parseMetadataSnapshotFromString(String xmlData, Metadata metadata) {
    try {
      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = builder.parse(new InputSource(new StringReader(xmlData)));
      document.getDocumentElement().normalize();
      DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
          MavenConstants.SNAPSHOT_LAST_UPDATED_DATE_TIME_FORMAT);
      LocalDateTime lastUpdated = LocalDateTime.parse(
          Objects.requireNonNull(getElementValue(document, MavenConstants.SNAPSHOT_LAST_UPDATED_TAG)),
          dateTimeFormatter);
      if (lastUpdated.equals(metadata.getLastUpdated())) {
        return;
      }
      metadata.setLastUpdated(lastUpdated);
      String value = document.getElementsByTagName(MavenConstants.VALUE_TAG).item(0).getTextContent();
      metadata.setSnapshotVersionValue(value);
    } catch (Exception e) {
      log.error("Metadata Reader: can not read the snapshot metadata of {}", xmlData);
    }
  }

  private static String getElementValue(Document doc, String tagName) {
    NodeList nodeList = doc.getElementsByTagName(tagName);
    if (nodeList.getLength() > 0) {
      return nodeList.item(0).getTextContent();
    }
    return null;
  }

  public static String downloadAndUnzipFile(String url, Metadata snapShotMetadata) throws IOException {
    String zipFilePath = snapShotMetadata.getArtifactId() + "." + MavenConstants.DEFAULT_PRODUCT_FOLDER_TYPE;
    downloadFile(url, zipFilePath);

    String destDir = snapShotMetadata.getArtifactId();
    unzip(zipFilePath, destDir);

    return Paths.get(destDir).toAbsolutePath().toString();
  }


  private static void downloadFile(String fileURL, String saveDir) throws IOException {
    URL url = new URL(fileURL);

    try (InputStream in = url.openStream()) {
      Path path = Paths.get(saveDir);
      Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  private static void unzip(String zipFilePath, String destDir) throws IOException {
    byte[] buffer = new byte[1024];

    Path unzipPath = Paths.get(destDir);
    if (!Files.exists(unzipPath)) {
      Files.createDirectories(unzipPath);
    }

    try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(Paths.get(zipFilePath)))) {
      ZipEntry zipEntry = zis.getNextEntry();
      while (zipEntry != null) {
        Path newPath = unzipPath.resolve(zipEntry.getName());

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
