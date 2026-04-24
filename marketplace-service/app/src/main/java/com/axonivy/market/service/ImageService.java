package com.axonivy.market.service;

import com.axonivy.market.core.entity.Image;
import com.axonivy.market.core.service.CoreImageService;
import org.kohsuke.github.GHContent;

import java.nio.file.Path;

public interface ImageService extends CoreImageService {

  /**
   * <p>
   * Retrieves the binary image data for a product image from GitHub. Downloads the image file from the
   * specified GitHub content using the provided download URL and returns the raw image bytes.
   * </p>
   *
   * @param  ghContent
   *              type {@link GHContent} - the GitHub file content object containing the image reference
   * @param  downloadUrl
   *              type {@link String} - the direct download URL for the image file from GitHub CDN
   * @return {@link byte[]} - the raw image binary data ready for storage or transmission; returns empty array
   *         if download fails
   * @author tvtphuc
   */
  byte[] getImageBinary(GHContent ghContent, String downloadUrl);

  /**
   * <p>
   * Creates or maps an Image entity from GitHub content metadata. Extracts image properties (name, path,
   * type) from the GitHub file content and creates an Image record associated with the specified product.
   * </p>
   *
   * @param  productId
   *              type {@link String} - the unique product identifier to associate the image with
   * @param  ghContent
   *              type {@link GHContent} - the GitHub file content object containing image file information
   * @return {@link Image} - the created Image entity with product association, file path, and metadata
   * @author nntthuy
   */
  Image mappingImageFromGHContent(String productId, GHContent ghContent);

  /**
   * <p>
   * Creates or maps an Image entity from a local file system path. Reads the image file content and checks
   * for existing images with identical binary data to avoid duplicates. If a matching image exists for the
   * product, returns the existing one; otherwise creates a new Image entity with the file content.
   * </p>
   *
   * @param  productId
   *              type {@link String} - the unique product identifier to associate the image with
   * @param  imagePath
   *              type {@link Path} - the local file system path to the image file to read and process
   * @return {@link Image} - the existing Image entity if content matches, or a new saved Image entity;
   *         returns null if file cannot be read or processed
   * @author ndkhanh
   */
  Image mappingImageFromDownloadedFolder(String productId, Path imagePath);

  /**
   * <p>
   * Reads and returns the binary data of a product image by its ID. Retrieves the image from local
   * storage cache or generates it on-demand, suitable for serving to web clients.
   * </p>
   *
   * @param  id
   *              type {@link String} - the unique image identifier in the database
   * @return {@link byte[]} - the raw image binary data; returns null if image not found or
   *         cannot be read from storage
   * @author tvtphuc
   */
  byte[] readImage(String id);

  /**
   * <p>
   * Reads and returns the binary data of a preview/thumbnail image by its filename. Retrieves scaled-down
   * versions of product images optimized for quick loading on listing pages and grids.
   * </p>
   *
   * @param  imageName
   *              type {@link String} - the filename of the preview image (e.g., "product-name-preview.jpg")
   * @return {@link byte[]} - the raw preview image binary data; returns empty array if image not found
   *         or cannot be read from storage
   * @author pvquan
   */
  byte[] readPreviewImageByName(String imageName);
}
