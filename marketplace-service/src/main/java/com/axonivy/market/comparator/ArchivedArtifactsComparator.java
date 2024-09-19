package com.axonivy.market.comparator;

import com.axonivy.market.github.model.ArchivedArtifact;

import java.util.Comparator;

public class ArchivedArtifactsComparator implements Comparator<ArchivedArtifact> {

  @Override
  public int compare(ArchivedArtifact artifact1, ArchivedArtifact artifact2) {
    return MavenVersionComparator.compare(artifact1.getLastVersion(), artifact2.getLastVersion());
  }
}