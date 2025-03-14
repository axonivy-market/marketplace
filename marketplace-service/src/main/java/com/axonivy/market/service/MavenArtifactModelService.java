package com.axonivy.market.service;

import com.axonivy.market.model.MavenModel;


public interface MavenArtifactModelService {

  MavenModel fetchMavenArtifactModels(String productId);

}
