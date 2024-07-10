package com.axonivy.market.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class XmlReaderUtilsTest {

  @Test
  void testExtractVersions() {
    List<String> versions = Collections.emptyList();
    XmlReaderUtils.extractVersions(StringUtils.EMPTY, versions);
    Assertions.assertTrue(versions.isEmpty());
  }
}
