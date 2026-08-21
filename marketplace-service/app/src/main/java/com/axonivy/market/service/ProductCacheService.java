package com.axonivy.market.service;

public interface ProductCacheService {

  /**
   * <p>
   * Checks whether the given product id belongs to an existing product, using an in-memory cache of all
   * known product ids that is refreshed periodically from the database. The cache is never queried by the
   * caller-supplied id directly, so unknown/forged ids cannot trigger a database lookup.
   * </p>
   *
   * @param productId type {@link String} - the product id to validate
   * @return {@code true} if the product id exists in the cache, {@code false} otherwise
   */
  boolean isValidProductId(String productId);

  /**
   * <p>
   * Checks whether the given product id and version combination exists, using the same periodically
   * refreshed in-memory cache described in {@link #isValidProductId(String)}.
   * </p>
   *
   * @param productId type {@link String} - the product id to validate
   * @param version   type {@link String} - the version to validate for the given product id
   * @return {@code true} if the id/version combination exists in the cache, {@code false} otherwise
   */
  boolean isValidProductIdAndVersion(String productId, String version);
}
