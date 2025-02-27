package com.axonivy.market.util;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.entity.Metadata;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MetadataReaderUtils {
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
      MavenConstants.DATE_TIME_FORMAT);
  private static final DateTimeFormatter SNAPSHOT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
      MavenConstants.SNAPSHOT_LAST_UPDATED_DATE_TIME_FORMAT);

  public static Metadata updateMetadataFromMavenXML(String xmlData, Metadata metadata,
      boolean isSnapShot) {
    Document document = getDocumentFromXMLContent(xmlData);
    try {
      LocalDateTime lastUpdated = getLastUpdatedTimeFromDocument(document, isSnapShot);
      if (lastUpdated.equals(metadata.getLastUpdated())) {
        return metadata;
      }
      metadata.setLastUpdated(lastUpdated);
      updateMetadataVersions(metadata, document, isSnapShot);
    } catch (Exception e) {
      log.error("Update metadata from maven failed {}", e.getMessage());
    }
    return metadata;
  }

  private static void updateMetadataVersions(Metadata metadata, Document document, boolean isSnapShot) {
    if (isSnapShot) {
      NodeList valueNodes = document.getElementsByTagName(MavenConstants.VALUE_TAG);
      String lastUpdatedTag = getElementValue(document, MavenConstants.SNAPSHOT_LAST_UPDATED_TAG);
      List<String> values = IntStream.range(0, valueNodes.getLength())
          .mapToObj(i -> valueNodes.item(i).getTextContent())
          .filter(text -> text.contains(Objects.requireNonNull(lastUpdatedTag))).toList();

      if (ObjectUtils.isNotEmpty(values)) {
        metadata.setSnapshotVersionValue(values.get(0));
      }
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
    String snapShotMetadataUrl = MavenUtils.buildSnapshotMetadataUrlFromArtifactInfo(mavenArtifact.getRepoUrl(),
        mavenArtifact.getGroupId(), mavenArtifact.getArtifactId(), version);
    String metadataContent = MavenUtils.getMetadataContentFromUrl(snapShotMetadataUrl);
    Document document = getDocumentFromXMLContent(metadataContent);
    return getElementValue(document, MavenConstants.VALUE_TAG);
  }

  public static Document getDocumentFromXMLContent(String xmlData) {
    Document document = null;
    try {
      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      document = builder.parse(new InputSource(new StringReader(xmlData)));
      document.getDocumentElement().normalize();
    } catch (Exception e) {
      log.error("Metadata Reader: can not read the metadata of {} with error", xmlData, e);
    }
    return document;
  }
}
