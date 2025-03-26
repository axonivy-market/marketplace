package com.axonivy.market.repository;

public interface CustomProductDesignerInstallationRepository {
  void increaseInstallationCountForProductByDesignerVersion(String productId, String designerVersion);
}
