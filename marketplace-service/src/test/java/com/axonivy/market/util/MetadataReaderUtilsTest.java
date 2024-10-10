package com.axonivy.market.util;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.entity.Metadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    Assertions.assertEquals(MOCK_SPRINT_RELEASED_VERSION, modifiedMetadata.getLatest());
    Assertions.assertEquals(MOCK_RELEASED_VERSION, modifiedMetadata.getRelease());
    Assertions.assertEquals(expectedLastUpdated, modifiedMetadata.getLastUpdated());
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
    Assertions.assertEquals("8.0.5-20221011.124215-170", metadata.getSnapshotVersionValue());
  }
}