package com.axonivy.market.service;

public interface ProductDependencyService {

  /**
   * <p>
   * Synchronizes IAR (AxonIvy Archive) dependencies for products by analyzing Maven POM files. Extracts
   * dependency information from product artifacts and creates or updates dependency relationships in the database.
   * Supports full reset sync to clear existing dependency data before re-synchronization.
   * </p>
   *
   * @param  resetSync
   *              type {@link Boolean} - if true, clears all existing dependency records before synchronization;
   *              if false, performs incremental update of changed dependencies only
   * @param  productId
   *              type {@link String} - specific product ID to sync dependencies for; if null or empty,
   *              synchronizes all products with artifacts
   * @return {@link int} - number of dependencies synchronized or updated; returns 0 if no dependencies found
   * @author nqhoan
   */
  int syncIARDependenciesForProducts(Boolean resetSync, String productId);
}
