package com.axonivy.market.comparator;

import com.axonivy.market.github.model.ArchivedArtifact;

import java.util.Comparator;

public class ArchivedArtifactsComparator implements Comparator<ArchivedArtifact> {
    private final LatestVersionComparator comparator = new LatestVersionComparator();
    
    @Override
    public int compare(ArchivedArtifact v1, ArchivedArtifact v2) {
        return comparator.compare(v1.getLastVersion(), v2.getLastVersion());
    }
}
