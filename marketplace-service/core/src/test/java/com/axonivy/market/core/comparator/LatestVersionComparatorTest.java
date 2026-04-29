package com.axonivy.market.core.comparator;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class LatestVersionComparatorTest {

  @Test
  void testComparatorBehavior() {
    LatestVersionComparator comparator = new LatestVersionComparator();

    // newer vs older
    int result = comparator.compare("10.0.0", "9.0.0");
    assertTrue(result < 0, "Newer version should come before older version");

    // release vs snapshot
    result = comparator.compare("1.0.0", "1.0.0-SNAPSHOT");
    assertTrue(result < 0, "Release should come before snapshot");

    // pre-release vs release
    result = comparator.compare("1.0.0", "1.0.0-alpha");
    assertTrue(result < 0, "Release should come before pre-release qualifiers");

    // sorting behavior
    List<String> versions = Arrays.asList("1.0.0", "2.0.0", "1.10.0", "1.2.0");
    List<String> sorted = versions.stream().sorted(comparator).toList();
    String[] expected = new String[]{"2.0.0", "1.10.0", "1.2.0", "1.0.0"};
    assertArrayEquals(expected, sorted.toArray(new String[0]), "Versions should be sorted with newest first");

    // Case 1: various pre-releases and a SNAPSHOT
    String[] case1 = ("13.2.0-a5,13.2.0-a6,13.2.0-a7,14.0.0-b1,14.0.0-b2,14.0.0-b3,14.0.0-SNAPSHOT").split(",");
    List<String> list1 = Arrays.asList(case1);
    List<String> sorted1 = list1.stream().sorted(comparator).toList();
    for (int i = 0; i < sorted1.size() - 1; i++) {
      assertTrue(comparator.compare(sorted1.get(i), sorted1.get(i + 1)) <= 0,
          "List 1 should be ordered newest-first at positions " + i + "," + (i + 1));
    }

    // Case 2: representative subset (not the full original list)
    String[] case2 = ("8.0.39,9.1.0.0-SNAPSHOT,9.4.0-m229,9.4.0,11.1.0-m240,11.1.0,14.0.0-SNAPSHOT").split(",");
    List<String> list2 = Arrays.asList(case2);
    List<String> sorted2 = list2.stream().sorted(comparator).toList();
    for (int i = 0; i < sorted2.size() - 1; i++) {
      assertTrue(comparator.compare(sorted2.get(i), sorted2.get(i + 1)) <= 0,
          "List 2 should be ordered newest-first at positions " + i + "," + (i + 1));
    }
  }
}
