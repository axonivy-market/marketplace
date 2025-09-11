package com.axonivy.market.util;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DocPathUtilsTest {

    private static final String SAMPLE_PATH = "/portal/portal-guide/13.1.1/doc/_images/dashboard1.png";

    @Test
    void extractProductId_validPath_returnsPortal() {
        assertEquals("portal", DocPathUtils.extractProductId(SAMPLE_PATH));
    }

    @Test
    void extractVersion_validPath_returnsVersion() {
        assertEquals("13.1.1", DocPathUtils.extractVersion(SAMPLE_PATH));
    }

    @Test
    void extractProductId_invalidPath_returnsNull() {
        assertNull(DocPathUtils.extractProductId("/invalidpath"));
    }

    @Test
    void extractVersion_invalidPath_returnsNull() {
        assertNull(DocPathUtils.extractVersion("/invalidpath"));
    }

    @Test
    void updateVersionInPath_replacesCorrectly() {
        String updated = DocPathUtils.updateVersionInPath(SAMPLE_PATH, "15.0", "13.1.1");
        assertTrue(updated.contains("/15.0/"));
        assertFalse(updated.contains("/13.1.1/"));
    }

    @Test
    void resolveDocPath_validPath_resolvesInsideBaseDir() {
        Path resolved = DocPathUtils.resolveDocPath("portal/portal-guide/13.1.1/doc/_images/dashboard1.png");
        assertNotNull(resolved);
        assertTrue(resolved.toString().contains("portal-guide"));
    }

    @Test
    void resolveDocPath_absoluteTraversalAttempt_returnsNull() {
        Path resolved = DocPathUtils.resolveDocPath("/../../etc/passwd");
        assertNull(resolved);
    }

}
