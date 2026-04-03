package com.axonivy.market.service;

public interface ProductDependencyService {

  /**
   * <p>
   * Synchronize IAR dependencies for products
   * </p>
   *
   * @param  resetSync
   *              type {@link Boolean}
   * @param  productId
   *              type {@link String}
   * @return {@link int}
   * @author nqhoan
   */
  int syncIARDependenciesForProducts(Boolean resetSync, String productId);
}
