package com.axonivy.market.util;

import com.axonivy.market.github.util.GitHubUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GitHubUtilsTest {
    @Test
    void testConvertArtifactIdToName() {
        String defaultArtifactId = "adobe-acrobat-sign-connector";
        String result = GitHubUtils.convertArtifactIdToName(defaultArtifactId);
        Assertions.assertEquals("Adobe Acrobat Sign Connector", result);

        result = GitHubUtils.convertArtifactIdToName(null);
        Assertions.assertEquals(StringUtils.EMPTY, result);

        result = GitHubUtils.convertArtifactIdToName(StringUtils.EMPTY);
        Assertions.assertEquals(StringUtils.EMPTY, result);

        result = GitHubUtils.convertArtifactIdToName(" ");
        Assertions.assertEquals(StringUtils.EMPTY, result);
    }
}
