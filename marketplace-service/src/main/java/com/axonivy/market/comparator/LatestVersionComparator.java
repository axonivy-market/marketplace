package com.axonivy.market.comparator;

import java.util.Comparator;

public class LatestVersionComparator implements Comparator<String> {

  @Override
  public int compare(String v1, String v2) {
    return MavenVersionComparator.compare(v2, v1);
  }
}
