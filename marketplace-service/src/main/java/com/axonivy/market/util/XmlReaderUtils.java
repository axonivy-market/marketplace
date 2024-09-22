package com.axonivy.market.util;

import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.maven.model.Metadata;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j2
public class XmlReaderUtils {
  private static final RestTemplate restTemplate = new RestTemplate();

  private XmlReaderUtils() {
  }

  public static List<String> readXMLFromUrl(String url) {
    List<String> versions = new ArrayList<>();
    try {
      String xmlData = restTemplate.getForObject(url, String.class);
      extractVersions(xmlData, versions);
    } catch (HttpClientErrorException e) {
      log.error(e.getMessage());
    }
    return versions;
  }

  public static void extractVersions(String xmlData, List<String> versions) {
    try {
      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = builder.parse(new InputSource(new StringReader(xmlData)));

      XPath xpath = XPathFactory.newInstance().newXPath();
      XPathExpression expr = xpath.compile(MavenConstants.VERSION_EXTRACT_FORMAT_FROM_METADATA_FILE);

      Object result = expr.evaluate(document, XPathConstants.NODESET);
      NodeList versionNodes = (NodeList) result;

      for (int i = 0; i < versionNodes.getLength(); i++) {
        versions.add(Optional.ofNullable(versionNodes.item(i)).map(Node::getTextContent).orElse(null));
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }

  public static void extractDataFromUrl(String url, Metadata metadata) {
    String xmlData = restTemplate.getForObject(url, String.class);
    try {
      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = builder.parse(new InputSource(new StringReader(xmlData)));
      document.getDocumentElement().normalize();
      metadata.setLatest(getElementValue(document, MavenConstants.LATEST_VERSION_TAG));
      metadata.setRelease(getElementValue(document, MavenConstants.LATEST_RELEASE_TAG));

      NodeList versionNodes = document.getElementsByTagName(MavenConstants.VERSION_TAG);
      for (int i = 0; i < versionNodes.getLength(); i++) {
        metadata.getVersions().add(versionNodes.item(i).getTextContent());
      }
      DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(MavenConstants.DATE_TIME_FORMAT);
      LocalDateTime lastUpdated = LocalDateTime.parse(getElementValue(document, MavenConstants.LAST_UPDATED_TAG), dateTimeFormatter);
      metadata.setLastUpdated(lastUpdated);
    } catch (Exception e) {
      log.error(e.getMessage());
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
