package com.axonivy.market.service;

import com.axonivy.market.model.DesignerInstallation;

import java.util.List;

public interface ProductDesignerInstallationService {

  /**
   * <p>
   * Retrieves the list of supported AxonIvy Designer versions for which a product can be installed.
   * Returns version information including Designer version number, compatibility flags, and installation
   * instructions specific to each Designer version.
   * </p>
   *
   * @param  productId
   *              type {@link String} - the unique product identifier to find Designer version installations for
   * @return {@link List<DesignerInstallation>} - list of Designer installation configurations for the product;
   *         returns empty list if product not found or has no Designer version support configured
   * @author phhhung
   */
  List<DesignerInstallation> findByProductId(String productId);
}
