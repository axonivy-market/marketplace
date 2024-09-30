package com.axonivy.market.util;

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
class MetadataReaderUtilsTest {
  public final String MOCK_SNAPSHOT = """
      <metadata modelVersion="1.1.0">
        <groupId>com.axonivy.demo</groupId>
        <artifactId>workflow-demos</artifactId>
        <version>8.0.5-SNAPSHOT</version>
        <versioning>
          <snapshot>
            <timestamp>20221011.124215</timestamp>
            <buildNumber>170</buildNumber>
          </snapshot>
        <lastUpdated>20221011130000</lastUpdated>
          <snapshotVersions>
             <snapshotVersion>
               <extension>iar</extension>
               <value>8.0.5-20221011.124215-170</value>
               <updated>20221011124215</updated>
             </snapshotVersion>
          </snapshotVersions>
        </versioning>
      </metadata>
       """;
  private final String MOCK_METADATA = """
      <metadata>
          <latest>1.0.2</latest>
          <release>1.0.1</release>
          <lastUpdated>20230924010101</lastUpdated>
          <versions>
              <version>1.0.0</version>
              <version>1.0.1</version>
              <version>1.0.2</version>
          </versions>
      </metadata>
      """;
  private static final String INVALID_METADATA = "<metadata><invalidTag></invalidTag></metadata>";

  private Metadata metadata;

  @BeforeEach
  void setUp() {
    metadata = Metadata.builder().build();
  }

  @Test
  void testUpdateMetadataFromReleasesMavenXML() {
    Metadata modifiedMetadata = MetadataReaderUtils.updateMetadataFromMavenXML(MOCK_METADATA, metadata, false);
    LocalDateTime expectedLastUpdated = LocalDateTime.parse("20230924010101",
        DateTimeFormatter.ofPattern(MavenConstants.DATE_TIME_FORMAT));

    Assertions.assertEquals("1.0.2", modifiedMetadata.getLatest());
    Assertions.assertEquals("1.0.1", modifiedMetadata.getRelease());
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
    MetadataReaderUtils.updateMetadataFromMavenXML(MOCK_SNAPSHOT, metadata, true);
    Assertions.assertEquals("8.0.5-20221011.124215-170", metadata.getSnapshotVersionValue());
  }
}