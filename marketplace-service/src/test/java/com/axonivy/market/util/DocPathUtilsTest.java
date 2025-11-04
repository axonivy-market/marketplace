package com.axonivy.market.util;

import com.axonivy.market.enums.DocumentLanguage;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DocPathUtilsTest {

    private static final String SAMPLE_PATH = "/portal/portal-guide/13.1.1/doc/_images/dashboard1.png";
    private static final String SAMPLE_DOC_FACTORY_PATH = "docfactory/doc-factory-doc/12/doc/index.html";

    @Test
    void testExtractProductIdSuccess() {
        assertEquals("portal", DocPathUtils.extractProductId(SAMPLE_PATH),
                "Should extract productId correctly");
    }

  @Test
  void testExtractProductIdForSpecialDocSuccess() {
    assertEquals("doc-factory", DocPathUtils.extractProductId(SAMPLE_DOC_FACTORY_PATH),
        "Should extract productId correctly");
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
    void testUpdateVersionAndLanguageInPathSuccess() {
        String updated = DocPathUtils.updateVersionAndLanguageInPath("portal", "portal-guide", "13.1.1",
            DocumentLanguage.ENGLISH);
        assertTrue(updated.contains("/portal/portal-guide/13.1.1/doc/en/index.html"), "Updated path should contain " +
            "new" +
            " version");
        assertFalse(updated.contains("/portal/portal-guide/13/doc/in/index.html"), "Updated path should not " +
            "contain old version");
    }

    @Test
    void testResolveDocPathSuccess() {
        Path resolved = DocPathUtils.resolveDocPath("portal/portal-guide/13.1.1/doc/_images/dashboard1.png");
        assertNotNull(resolved, "Resolved path should not be null");
        assertTrue(resolved.toString().contains("portal-guide"), "Resolved path should contain 'portal-guide'");
    }

    @Test
    void testResolveDocPathFailed() {
        Path resolved = DocPathUtils.resolveDocPath("/../../etc/passwd");
        assertNull(resolved, "Path traversal should return null");
    }

    @Test
    void testExtractLanguageFromPath() {
        String path = "/portal/portal-guide/13.1.1/doc/en/_images/dashboard1.png";
        DocumentLanguage language = DocPathUtils.extractLanguage(path);
        assertEquals(DocumentLanguage.ENGLISH, language, "Should extract language correctly");
    }

    @Test
    void testExtractLanguageFromPathNoLanguage() {
      DocumentLanguage language = DocPathUtils.extractLanguage(SAMPLE_PATH);
        assertNull(language, "Should return null when no language is present");
    }

    @Test
    void testExtractArtifactNameSuccess() {
        assertEquals("portal-guide", DocPathUtils.extractArtifactName(SAMPLE_PATH),
                "Should extract artifact name correctly");
    }

    @Test
    void testExtractArtifactNameNoArtifact() {
        String path = "dashboard1.png";
        String artifactName = DocPathUtils.extractArtifactName(path);
        assertNull(artifactName, "Should return null when no artifact name is present");
    }

}
