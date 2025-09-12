package com.axonivy.market.repository.impl;

import com.axonivy.market.entity.ProductDesignerInstallation;
import lombok.Getter;

@Getter
public class TestableCustomProductDesignerInstallationRepositoryImpl extends CustomProductDesignerInstallationRepositoryImpl {
  private ProductDesignerInstallation captured;

  @Override
  protected void save(ProductDesignerInstallation entity) {
    this.captured = entity; // capture instead of persisting
  }
}
