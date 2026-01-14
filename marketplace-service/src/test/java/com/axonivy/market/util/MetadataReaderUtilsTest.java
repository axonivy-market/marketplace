package com.axonivy.market.util;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.entity.Artifact;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.entity.Metadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetadataReaderUtilsTest extends BaseSetup {
  private static final String INVALID_METADATA = "<metadata><invalidTag></invalidTag></metadata>";

  private Metadata metadata;

  @BeforeEach
  void setUp() {
    metadata = Metadata.builder().build();
  }

  @Test
  void testUpdateMetadataFromReleasesMavenXML() {
    Metadata modifiedMetadata = MetadataReaderUtils.updateMetadataFromMavenXML(getMockMetadataContent(), metadata,
        false);
    LocalDateTime expectedLastUpdated = LocalDateTime.parse("20230924010101",
        DateTimeFormatter.ofPattern(MavenConstants.DATE_TIME_FORMAT));

    assertEquals(MOCK_SPRINT_RELEASED_VERSION, modifiedMetadata.getLatest(),
        "Metadata latest version should match sprint released version");
    assertEquals(MOCK_RELEASED_VERSION, modifiedMetadata.getRelease(),
        "Metadata released should match released version");
    assertEquals(expectedLastUpdated, modifiedMetadata.getLastUpdated(),
        "Metadata last updated date should match expected last updated date");
  }

  @Test
  void testUpdateMetadataFromInvalidSnapshotMavenXML() {
    MetadataReaderUtils.updateMetadataFromMavenXML(INVALID_METADATA, metadata, true);
    Assertions.assertNull(metadata.getLatest(),
        "Metadata latest version should be null if metadata is invalid");
    Assertions.assertNull(metadata.getRelease(),
        "Metadata release should be null if metadata is invalid");
    Assertions.assertNull(metadata.getLastUpdated(),
        "Metadata last updated date should be null if metadata is invalid");
  }

  @Test
  void testUpdateMetadataFromSnapshotXml() {
    MetadataReaderUtils.updateMetadataFromMavenXML(getMockSnapshotMetadataContent(), metadata, true);
    assertEquals("12.0.2-20250224.083844-2", metadata.getSnapshotVersionValue(),
        "Metadata snapshot version should be match input");
  }

  @Test
  void testGetVersionValueFormMetadataUrl() {
    try (MockedStatic<HttpFetchingUtils> mockHttpUtils = Mockito.mockStatic(HttpFetchingUtils.class)) {
      String mockMetadataUrl = "http://example.com/maven/metadata.xml";
      mockHttpUtils.when(() -> HttpFetchingUtils.getFileAsString(mockMetadataUrl)).thenReturn(
          getMockSnapshotMetadataContent());
      String snapshotVersionValue = MetadataReaderUtils.getVersionValueFormMetadataUrl(mockMetadataUrl);
      assertEquals("8.0.5-20221011.124215-170", snapshotVersionValue,
          "Metadata snapshot version should be match input");
    }
  }
}