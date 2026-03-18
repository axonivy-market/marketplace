package com.axonivy.market.core.respository.impl;

import com.axonivy.market.core.entity.ProductDesignerInstallation;
import com.axonivy.market.core.repository.impl.CoreCustomProductDesignerInstallationRepositoryImpl;
import lombok.Getter;

@Getter
public class TestableCoreCustomProductDesignerInstallationRepositoryImpl extends CoreCustomProductDesignerInstallationRepositoryImpl {
  private ProductDesignerInstallation captured;

  @Override
  protected void save(ProductDesignerInstallation entity) {
    this.captured = entity; // capture instead of persisting
  }
}
