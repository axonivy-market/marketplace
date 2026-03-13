package com.axonivy.market.util;

import com.axonivy.market.enums.DocumentLanguage;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocPathUtilsTest {

  private static final String SAMPLE_PATH = "/portal/portal-guide/13.1.1/doc/_images/dashboard1.png";
  private static final String SAMPLE_DOC_FACTORY_PATH = "docfactory/doc-factory-doc/12/doc/index.html";
  private static final String SAMPLE_DOC_FACTORY_DEV_PATH = "doc-factory/doc-factory-doc/dev/doc/index.html";
  private static final String SAMPLE_PORTAL_PATH = "/portal/portal-guide/13.1.1/doc/en/index.html";
  private static final String ARTIFACT = "portal-guide";
  private static final String PORTAL = "portal";
  private static final String DOC_FACTORY = "docfactory";

  @Test
  void testExtractProductIdSuccess() {
    assertEquals(PORTAL, DocPathUtils.extractProductId(SAMPLE_PATH),
        "Should extract productId correctly");
  }

  @Test
  void testExtractProductIdForDocFactorySuccess() {
    assertEquals(DOC_FACTORY, DocPathUtils.extractProductId(SAMPLE_DOC_FACTORY_DEV_PATH),
        "Should extract productId correctly - docfactory");
  }

  @Test
  void testExtractProductIdForSpecialDocSuccess() {
    assertEquals(DOC_FACTORY, DocPathUtils.extractProductId(SAMPLE_DOC_FACTORY_PATH),
        "Should extract productId correctly");
  }

  @Test
  void testGetProductName() {
    assertEquals("doc-factory", DocPathUtils.getProductName(DOC_FACTORY),
        "Should convert docfactory to doc-factory");
    assertEquals("some-product", DocPathUtils.getProductName("some-product"),
        "Should return the same product name if not docfactory");
  }

  @Test
  void testExtractVersionSuccess() {
    assertEquals("13.1.1", DocPathUtils.extractVersion(SAMPLE_PATH),
        "Should extract version correctly");
  }

  @Test
  void testExtractProductIdWithInvalidPath() {
    assertNull(DocPathUtils.extractProductId("/invalidPath"), "Should return null");
  }

  @Test
  void testExtractVersionWithInvalidPath() {
    assertNull(DocPathUtils.extractVersion("/invalidPath"), "Should return null");
  }

  @Test
  void testGeneratePathSuccess() {
    String updated = DocPathUtils.generatePath(PORTAL,
        ARTIFACT, "13.1.1",
        DocumentLanguage.ENGLISH);
    assertTrue(updated.contains(SAMPLE_PORTAL_PATH),
        "Updated path should contain " + "new" + " version");
    assertFalse(updated.contains("/portal/portal-guide/13/doc/in/index.html"),
        "Updated path should not contain old version");
  }

  @Test
  void testExtractLanguageFromPath() {
    DocumentLanguage language = DocPathUtils.extractLanguage(SAMPLE_PORTAL_PATH);
    assertEquals(DocumentLanguage.ENGLISH, language, "Should extract language correctly");
  }

  @Test
  void testExtractLanguageFromPathNoLanguage() {
    DocumentLanguage language = DocPathUtils.extractLanguage(SAMPLE_PATH);
    assertNull(language, "Should return null when no language is present");
  }

  @Test
  void testExtractArtifactNameSuccess() {
    assertEquals(ARTIFACT, DocPathUtils.extractArtifactName(SAMPLE_PATH),
        "Should extract artifact name correctly");
  }

  @Test
  void testExtractArtifactNameNoArtifact() {
    String path = "dashboard1.png";
    String artifactName = DocPathUtils.extractArtifactName(path);
    assertNull(artifactName, "Should return null when no artifact name is present");
  }

  @Test
  void testCreateArtifactNameByProductNameWithEmptyProductName() {
    String artifactName = DocPathUtils.createArtifactNameByProductName(StringUtils.EMPTY);
    assertNull(artifactName, "Should return null when product name is empty");
  }

  @Test
  void testCreateArtifactNameByProductName() {
    String productName = "product";
    String artifactName = DocPathUtils.createArtifactNameByProductName(productName);
    assertEquals(productName + DocPathUtils.DOC_EXTENSION, artifactName,
        "Should return null when product name is empty");

    artifactName = DocPathUtils.createArtifactNameByProductName(DocPathUtils.DOC_FACTORY_DOC);
    assertEquals(DocPathUtils.DOC_FACTORY_ID + DocPathUtils.DOC_EXTENSION, artifactName,
        "Should return doc-factory-doc when product name is docfactory");

    artifactName = DocPathUtils.createArtifactNameByProductName(PORTAL);
    assertEquals(ARTIFACT, artifactName, "Should return portal-guide when product name is portal");
  }
}
