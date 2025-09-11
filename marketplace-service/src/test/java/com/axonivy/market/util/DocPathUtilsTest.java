package com.axonivy.market.util;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DocPathUtilsTest {

    private static final String SAMPLE_PATH = "/portal/portal-guide/13.1.1/doc/_images/dashboard1.png";

    @Test
    void testExtractProductIdSuccess() {
        assertEquals("portal", DocPathUtils.extractProductId(SAMPLE_PATH));
    }

    @Test
    void testExtractVersionSuccess() {
        assertEquals("13.1.1", DocPathUtils.extractVersion(SAMPLE_PATH));
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
    void testUpdateVersionInPathSuccess() {
        String updated = DocPathUtils.updateVersionInPath(SAMPLE_PATH, "15.0", "13.1.1");
        assertTrue(updated.contains("/15.0/"));
        assertFalse(updated.contains("/13.1.1/"));
    }

    @Test
    void testResolveDocPathSuccess() {
        Path resolved = DocPathUtils.resolveDocPath("portal/portal-guide/13.1.1/doc/_images/dashboard1.png");
        assertNotNull(resolved);
        assertTrue(resolved.toString().contains("portal-guide"));
    }

    @Test
    void testResolveDocPathFailed() {
        Path resolved = DocPathUtils.resolveDocPath("/../../etc/passwd");
        assertNull(resolved);
    }

}
