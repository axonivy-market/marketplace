package com.axonivy.market.service;

import com.axonivy.market.model.DesignerInstallation;

import java.util.List;

public interface ProductDesignerInstallationService {
  List<DesignerInstallation> findByProductId(String productId);
}
