package com.axonivy.market.util;

import com.axonivy.market.entity.Metadata;
import com.axonivy.market.constants.MavenConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ExtendWith(MockitoExtension.class)
class MetadataReaderUtilsTest {
  private static final String MOCK_METADATA = """
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
  private static final String MOCK_SNAPSHOT = """
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
  private static final String INVALID_METADATA = "<metadata><invalidTag></invalidTag></metadata>";

  private Metadata metadata;

  @BeforeEach
  void setUp() {
    metadata = Metadata.builder().build();
  }

  @Test
  void testParseMetadataFromStringWithValidXml() {
    MetadataReaderUtils.parseMetadataFromString(MOCK_METADATA, metadata);
    LocalDateTime expectedLastUpdated = LocalDateTime.parse("20230924010101",
        DateTimeFormatter.ofPattern(MavenConstants.DATE_TIME_FORMAT));

    Assertions.assertEquals("1.0.2", metadata.getLatest());
    Assertions.assertEquals("1.0.1", metadata.getRelease());
    Assertions.assertEquals(expectedLastUpdated, metadata.getLastUpdated());
  }

  @Test
  public void testParseInvalidXML() throws Exception {
    MetadataReaderUtils.parseMetadataSnapshotFromString(INVALID_METADATA, metadata);
    Assertions.assertNull(metadata.getLatest());
    Assertions.assertNull(metadata.getRelease());
    Assertions.assertNull(metadata.getLastUpdated());
  }

  @Test
  public void testParseMetadataSnapshotFromStringWithValidXml() throws Exception {
    MetadataReaderUtils.parseMetadataSnapshotFromString(MOCK_SNAPSHOT, metadata);
    Assertions.assertEquals("8.0.5-20221011.124215-170", metadata.getSnapshotVersionValue());
  }
}