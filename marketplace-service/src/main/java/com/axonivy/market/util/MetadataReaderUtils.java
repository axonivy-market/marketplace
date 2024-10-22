package com.axonivy.market.util;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.entity.Metadata;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MetadataReaderUtils {
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
      MavenConstants.DATE_TIME_FORMAT);
  private static final DateTimeFormatter SNAPSHOT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
      MavenConstants.SNAPSHOT_LAST_UPDATED_DATE_TIME_FORMAT);

  public static Metadata updateMetadataFromMavenXML(String xmlData, Metadata metadata, boolean isSnapShot) {
    try {
      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = builder.parse(new InputSource(new StringReader(xmlData)));
      document.getDocumentElement().normalize();
      LocalDateTime lastUpdated = getLastUpdatedTimeFromDocument(document, isSnapShot);
      if (lastUpdated.equals(metadata.getLastUpdated())) {
        return metadata;
      }
      metadata.setLastUpdated(lastUpdated);
      updateMetadataVersions(metadata, document, isSnapShot);
    } catch (Exception e) {
      log.error("Metadata Reader: can not read the metadata of {} with error", xmlData, e);
    }
    return metadata;
  }

  private static void updateMetadataVersions(Metadata metadata, Document document, boolean isSnapShot) {
    if (isSnapShot) {
      String value = document.getElementsByTagName(MavenConstants.VALUE_TAG).item(0).getTextContent();
      metadata.setSnapshotVersionValue(value);
      return;
    }
    metadata.setLatest(getElementValue(document, MavenConstants.LATEST_VERSION_TAG));
    metadata.setRelease(getElementValue(document, MavenConstants.LATEST_RELEASE_TAG));
    NodeList versionNodes = document.getElementsByTagName(MavenConstants.VERSION_TAG);
    for (int i = 0; i < versionNodes.getLength(); i++) {
      metadata.getVersions().add(versionNodes.item(i).getTextContent());
    }
  }

  private static LocalDateTime getLastUpdatedTimeFromDocument(Document document, boolean isSnapShot) {
    String textValue = isSnapShot ? Objects.requireNonNull(getElementValue(document,
        MavenConstants.SNAPSHOT_LAST_UPDATED_TAG)) : Objects.requireNonNull(getElementValue(document,
        MavenConstants.LAST_UPDATED_TAG));
    DateTimeFormatter lastUpdatedFormatter = isSnapShot ? SNAPSHOT_DATE_TIME_FORMATTER : DATE_TIME_FORMATTER;
    return LocalDateTime.parse(textValue, lastUpdatedFormatter);
  }

  public static String getElementValue(Document doc, String tagName) {
    NodeList nodeList = doc.getElementsByTagName(tagName);
    if (nodeList.getLength() > 0) {
      return nodeList.item(0).getTextContent();
    }
    return null;
  }

  public static String getSnapshotVersionValue(String version,
      Artifact mavenArtifact) {
    String snapshotVersionValue = Strings.EMPTY;
    String snapShotMetadataUrl = MavenUtils.buildSnapshotMetadataUrlFromArtifactInfo(mavenArtifact.getRepoUrl(),
        mavenArtifact.getGroupId(), mavenArtifact.getArtifactId(), version);
    String metadataContent = MavenUtils.getMetadataContentFromUrl(snapShotMetadataUrl);
    try {
      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = builder.parse(new InputSource(new StringReader(metadataContent)));
      document.getDocumentElement().normalize();
      snapshotVersionValue = getElementValue(document, MavenConstants.VALUE_TAG);
    } catch (Exception e) {
      log.error("Cannot get snapshot version value from maven {}", e.getMessage());
    }
    return snapshotVersionValue;
  }
}
