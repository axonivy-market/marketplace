package com.axonivy.market.util;

import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.entity.Metadata;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Log4j2
public class MetadataReaderUtils {
  private static final String ZIP_FILE_FORMAT = "%s.%s";
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
    String zipFilePath =
        String.format(ZIP_FILE_FORMAT, snapShotMetadata.getArtifactId(), MavenConstants.DEFAULT_PRODUCT_FOLDER_TYPE);
    FileUnzipUtils.downloadFile(url, zipFilePath);

    String destDir = snapShotMetadata.getArtifactId();
    FileUnzipUtils.unzipFile(zipFilePath, destDir);
    Files.deleteIfExists(Path.of(zipFilePath));
    return Paths.get(destDir).toAbsolutePath().toString();
  }
}
