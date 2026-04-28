package com.axonivy.market.core.service;

public interface CoreImageService {

  /**
   * <p>
   * Reads and returns the binary image data for a product image by its ID.
   * </p>
   *
   * @param  id
   *              type {@link String} - the unique image identifier in the database
   * @return {@link byte[]} - the raw image binary data; returns empty array if image not found or cannot
   *         be read from storage
   * @author ntqdinh
   */
  byte[] readImage(String id);
}
