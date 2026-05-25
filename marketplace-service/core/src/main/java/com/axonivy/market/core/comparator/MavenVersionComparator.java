package com.axonivy.market.core.comparator;

import org.apache.maven.artifact.versioning.ComparableVersion;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator for version strings using Maven natural ordering.
 *
 * This comparator delegates to {@link org.apache.maven.artifact.versioning.ComparableVersion}
 * and returns results consistent with natural version comparison. Use
 * {@link Comparator#reversed()} when callers need newest versions before older ones.
 *
 */
public class MavenVersionComparator implements Comparator<String>, Serializable {
  @Serial
  private static final long serialVersionUID = 1;

  private static MavenVersionComparator instance;

  public static MavenVersionComparator getInstance() {
    if (instance == null) {
      instance = new MavenVersionComparator();
    }
    return instance;
  }

  @Override
  public int compare(String v1, String v2) {
    ComparableVersion cv1 = new ComparableVersion(v1);
    ComparableVersion cv2 = new ComparableVersion(v2);
    return cv1.compareTo(cv2);
  }
}
