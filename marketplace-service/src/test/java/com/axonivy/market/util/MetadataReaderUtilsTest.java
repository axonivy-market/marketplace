package com.axonivy.market.util;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.bo.Artifact;
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

    assertEquals(MOCK_SPRINT_RELEASED_VERSION, modifiedMetadata.getLatest());
    assertEquals(MOCK_RELEASED_VERSION, modifiedMetadata.getRelease());
    assertEquals(expectedLastUpdated, modifiedMetadata.getLastUpdated());
  }

  @Test
  void testUpdateMetadataFromInvalidSnapshotMavenXML() {
    MetadataReaderUtils.updateMetadataFromMavenXML(INVALID_METADATA, metadata, true);
    Assertions.assertNull(metadata.getLatest());
    Assertions.assertNull(metadata.getRelease());
    Assertions.assertNull(metadata.getLastUpdated());
  }

  @Test
  void testUpdateMetadataFromSnapshotXml() {
    MetadataReaderUtils.updateMetadataFromMavenXML(getMockSnapShotMetadataContent(), metadata, true);
    assertEquals("8.0.5-20221011.124215-170", metadata.getSnapshotVersionValue());
  }

  @Test
  void testGetSnapshotVersionValue() {
    try (MockedStatic<MavenUtils> mockUtils = Mockito.mockStatic(MavenUtils.class)) {
      Artifact mockArtifact = mock(Artifact.class);

      // Mock Artifact properties
      when(mockArtifact.getRepoUrl()).thenReturn("http://example.com/maven");
      when(mockArtifact.getGroupId()).thenReturn("com.example");
      when(mockArtifact.getArtifactId()).thenReturn("example-artifact");

      String mockMetadataUrl = "http://example.com/maven/metadata.xml";
      String mockMetadataContent = "<metadata><value>1.0.0-20230101.123456-1</value></metadata>";

      when(MavenUtils.buildSnapshotMetadataUrlFromArtifactInfo("http://example.com/maven", "com.example",
          "example-artifact", MOCK_SNAPSHOT_VERSION))
          .thenReturn(mockMetadataUrl);
      when(MavenUtils.getMetadataContentFromUrl(mockMetadataUrl)).thenReturn(mockMetadataContent);

      String snapshotVersionValue = MetadataReaderUtils.getSnapshotVersionValue(MOCK_SNAPSHOT_VERSION, mockArtifact);

      assertEquals("1.0.0-20230101.123456-1", snapshotVersionValue);
    }
  }
}