package com.axonivy.market.util;

import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.entity.Metadata;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Log4j2
public class MetadataReaderUtils {

  private MetadataReaderUtils() {
  }

  public static void parseMetadataFromString(String xmlData, Metadata metadata) {
    try {
      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = builder.parse(new InputSource(new StringReader(xmlData)));
      document.getDocumentElement().normalize();
      metadata.setLatest(getElementValue(document, MavenConstants.LATEST_VERSION_TAG));
      metadata.setRelease(getElementValue(document, MavenConstants.LATEST_RELEASE_TAG));

      DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(MavenConstants.DATE_TIME_FORMAT);
      LocalDateTime lastUpdated = LocalDateTime.parse(getElementValue(document, MavenConstants.LAST_UPDATED_TAG),
          dateTimeFormatter);
      metadata.setLastUpdated(lastUpdated);
      NodeList versionNodes = document.getElementsByTagName(MavenConstants.VERSION_TAG);
      for (int i = 0; i < versionNodes.getLength(); i++) {
        metadata.getVersions().add(versionNodes.item(i).getTextContent());
      }
    } catch (Exception e) {
      log.error("Metadata Reader: can not read the metadata of {}", xmlData);
    }
  }

  private static String getElementValue(Document doc, String tagName) {
    NodeList nodeList = doc.getElementsByTagName(tagName);
    if (nodeList.getLength() > 0) {
      return nodeList.item(0).getTextContent();
    }
    return null;
  }
}
