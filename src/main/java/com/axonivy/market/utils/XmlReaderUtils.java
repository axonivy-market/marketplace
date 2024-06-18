package com.axonivy.market.utils;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j2
public class XmlReaderUtils {

    private static final String VERSION_PATH = "//versions/version/text()";
    private static final RestTemplate restTemplate = new RestTemplate();

    public static List<String> readXMLFromUrl(String url) {
        List<String> versions = new ArrayList<>();
        try {
            String xmlData = restTemplate.getForObject(url, String.class);
            extractVersions(xmlData, versions);
        } catch (HttpClientErrorException | ParserConfigurationException |
                 IOException | SAXException | XPathExpressionException e) {
            log.error("Can not read the content from this url: {} {}", url, e);
        }
        return versions;
    }

    private static void extractVersions(String xmlData, List<String> versions) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xmlData)));
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xpath.compile(VERSION_PATH);
        Object result = expr.evaluate(document, XPathConstants.NODESET);
        NodeList versionNodes = (NodeList) result;
        for (int i = 0; i < versionNodes.getLength(); i++) {
            versions.add(Optional.ofNullable(versionNodes.item(i)).map(Node::getTextContent).orElse(null));
        }
    }
}
