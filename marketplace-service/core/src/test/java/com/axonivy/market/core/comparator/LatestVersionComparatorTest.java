package com.axonivy.market.core.comparator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LatestVersionComparatorTest {

  @Test
  void testCompareLatestVersionFirst() {
    LatestVersionComparator comparator = new LatestVersionComparator();
    int result = comparator.compare("10.0.0", "9.0.0");
    assertTrue(result < 0, "Newer version should come before older version");
  }
}
