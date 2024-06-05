package com.axonivy.market.service;

import com.axonivy.market.model.MavenArtifactModel;

public interface ArtifactService {
    MavenArtifactModel getArtifactFromRepoNameAndVersion();
}
