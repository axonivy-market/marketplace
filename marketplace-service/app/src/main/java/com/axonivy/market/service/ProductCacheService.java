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
}
