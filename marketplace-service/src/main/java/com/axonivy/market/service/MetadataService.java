package com.axonivy.market.service;

public interface MetadataService {
  boolean syncAllArtifactFromMaven();

  void clearAllSync();
}
