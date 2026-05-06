package com.axonivy.market.core.comparator;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class MavenVersionComparatorTest {

  @Test
  void testCompare() {
    MavenVersionComparator comparator = MavenVersionComparator.getInstance();

    // Compare: newer vs older
    int result = comparator.compare("10.0.0", "9.0.0");
    assertTrue(result < 0, "Newer version should come before older version");

    // Compare: release vs snapshot
    result = comparator.compare("1.0.0", "1.0.0-SNAPSHOT");
    assertTrue(result < 0, "Release should come before snapshot");

    // Compare: pre-release vs release
    result = comparator.compare("1.0.0", "1.0.0-alpha");
    assertTrue(result < 0, "Release should come before pre-release qualifiers");

    // Sorting behavior
    List<String> versions = Arrays.asList("1.0.0", "2.0.0", "1.10.0", "1.2.0");
    List<String> sortedVersions = versions.stream().sorted(comparator).toList();
    String[] expectedVersions = new String[]{"2.0.0", "1.10.0", "1.2.0", "1.0.0"};
    assertArrayEquals(expectedVersions, sortedVersions.toArray(new String[0]),
        "Versions should be sorted with newest first");

    // Case 1: various pre-releases and a SNAPSHOT
    String[] case1 = ("13.2.0-a5,13.2.0-a6,13.2.0-a7,14.0.0-b1,14.0.0-b2,14.0.0-b3,14.0.0-SNAPSHOT").split(",");
    List<String> listVersions1 = Arrays.asList(case1);
    List<String> sortedVersions1 = listVersions1.stream().sorted(comparator).toList();
    for (int i = 0; i < sortedVersions1.size() - 1; i++) {
      assertTrue(comparator.compare(sortedVersions1.get(i), sortedVersions1.get(i + 1)) <= 0,
          "List 1 should be ordered newest-first at positions " + i + "," + (i + 1));
    }

    // Case 2: representative subset (not the full original list)
    String[] case2 = ("8.0.39,9.1.0.0-SNAPSHOT,9.4.0-m229,9.4.0,11.1.0-m240,11.1.0,14.0.0-SNAPSHOT").split(",");
    List<String> listVersions2 = Arrays.asList(case2);
    List<String> sortedVersions2 = listVersions2.stream().sorted(comparator).toList();
    for (int i = 0; i < sortedVersions2.size() - 1; i++) {
      assertTrue(comparator.compare(sortedVersions2.get(i), sortedVersions2.get(i + 1)) <= 0,
          "List 2 should be ordered newest-first at positions " + i + "," + (i + 1));
    }
  }
}
