package com.axonivy.market.util;

import com.axonivy.market.entity.Metadata;
import com.axonivy.market.constants.MavenConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class MetadataReaderUtilsTest {
  private static final String VALID_XML = """
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
  private Metadata metadata;

  @BeforeEach
  void setUp() {
    metadata = mock(Metadata.class);
  }

  @Test
  void testParseMetadataFromStringWithValidXml() {
    MetadataReaderUtils.parseMetadataFromString(VALID_XML, metadata);

    verify(metadata).setLatest("1.0.2");
    verify(metadata).setRelease("1.0.1");

    LocalDateTime expectedLastUpdated = LocalDateTime.parse("20230924010101",
        DateTimeFormatter.ofPattern(MavenConstants.DATE_TIME_FORMAT));
    verify(metadata).setLastUpdated(expectedLastUpdated);

    verify(metadata, Mockito.times(3)).getVersions();
  }

  @Test
  void testParseMetadataFromStringWithInvalidXml() {
    String invalidXml = "<metadata><invalid></metadata>"; // malformed XML

    MetadataReaderUtils.parseMetadataFromString(invalidXml, metadata);

    verify(metadata, Mockito.never()).setLatest(Mockito.anyString());
    verify(metadata, Mockito.never()).setRelease(Mockito.anyString());
    verify(metadata, Mockito.never()).setLastUpdated(Mockito.any(LocalDateTime.class));
    verify(metadata, Mockito.never()).getVersions();
  }
}