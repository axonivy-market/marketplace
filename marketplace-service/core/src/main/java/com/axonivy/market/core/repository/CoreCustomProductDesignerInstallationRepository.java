package com.axonivy.market.core.repository;

public interface CoreCustomProductDesignerInstallationRepository {
  void increaseInstallationCountForProductByDesignerVersion(String productId, String designerVersion);
}
