package com.axonivy.market.core.comparator;

import org.apache.maven.artifact.versioning.ComparableVersion;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator for version strings that orders "latest" first.
 *
 * This comparator delegates to {@link org.apache.maven.artifact.versioning.ComparableVersion}
 * and returns results so that the newest version appears before older ones (suitable for
 * passing into collection sort APIs like `.sorted(LatestVersionComparator.getInstance())`).
 *
 */
public class LatestVersionComparator implements Comparator<String>, Serializable {
  @Serial
  private static final long serialVersionUID = 1;

  private static LatestVersionComparator instance;

  public static LatestVersionComparator getInstance() {
    if (instance == null) {
      instance = new LatestVersionComparator();
    }
    return instance;
  }

  @Override
  public int compare(String v1, String v2) {
    ComparableVersion cv1 = new ComparableVersion(v1);
    ComparableVersion cv2 = new ComparableVersion(v2);
    return cv2.compareTo(cv1);
  }
}
