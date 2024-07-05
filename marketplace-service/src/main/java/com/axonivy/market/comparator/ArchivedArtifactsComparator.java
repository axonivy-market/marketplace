package com.axonivy.market.comparator;

import com.axonivy.market.github.model.ArchivedArtifact;

import java.util.Comparator;

public class ArchivedArtifactsComparator implements Comparator<ArchivedArtifact> {
	private final LatestVersionComparator comparator = new LatestVersionComparator();

	@Override
	public int compare(ArchivedArtifact artifact1, ArchivedArtifact artifact2) {
		return comparator.compare(artifact1.getLastVersion(), artifact2.getLastVersion());
	}
}