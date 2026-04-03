package com.axonivy.market.service;

import com.axonivy.market.model.DesignerInstallation;

import java.util.List;

public interface ProductDesignerInstallationService {

  /**
   * <p>
   * Find Ivy designer version by product id
   * </p>
   *
   * @param  productId
   *              type {@link String}
   * @return {@link }
   * @author phhhung
   */
  List<DesignerInstallation> findByProductId(String productId);
}
