package com.axonivy.market.comparator;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;

public class LatestVersionComparator implements Comparator<String>, Serializable {
  @Serial
  private static final long serialVersionUID = 1;

  @Override
  public int compare(String v1, String v2) {
    return MavenVersionComparator.compare(v2, v1);
  }
}
