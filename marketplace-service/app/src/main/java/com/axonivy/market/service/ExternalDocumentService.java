package com.axonivy.market.service;

import com.axonivy.market.entity.ExternalDocumentMeta;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.model.DocumentInfoResponse;

import java.util.List;

public interface ExternalDocumentService {

  /**
   * <p>
   * Synchronizes external documentation for a product by downloading documentation artifacts from Maven
   * repositories and storing metadata in the database. Optionally syncs a specific product version or
   * all released versions. Supports full reset sync to clear existing metadata before synchronization.
   * Only processes products with released versions and available documentation artifacts.
   * </p>
   *
   * @param  productId
   *              type {@link String} - the unique identifier of the product to synchronize documentation for
   * @param  isResetSync
   *              type {@link boolean} - if true, existing documentation metadata for the product will be
   *              deleted before synchronization; if false, only missing versions are synchronized
   * @param  version
   *              type {@link String} - specific product version to synchronize (optional); if blank, all
   *              released versions will be synchronized
   * @return void - no return value; synchronization results are persisted in the ExternalDocumentMeta repository
   * @author nqhoan
   */
  void syncDocumentForProduct(String productId, boolean isResetSync, String version);

  /**
   * <p>
   * Retrieves all products that have documentation artifacts configured in the system. A product
   * is considered to have documentation if it contains at least one artifact with the 'doc' flag
   * enabled. This is typically used to identify products eligible for external document synchronization.
   * </p>
   *
   * @return {@link List<Product>} - list of all products that have documentation artifacts; returns empty
   *         list if no products with documentation are found
   * @author nqhoan
   */
  List<Product> findAllProductsHaveDocument();

  /**
   * <p>
   * Finds the external documentation metadata for a specific product and version. Resolves the best
   * matching version if an exact match is not found, prioritizing English language documentation.
   * Returns null if the product does not exist or no suitable documentation is available.
   * </p>
   *
   * @param  productId
   *              type {@link String} - the unique identifier of the product to find documentation for
   * @param  version
   *              type {@link String} - the requested product version; if no exact match exists, the
   *              best available version is resolved using version factory logic
   * @return {@link ExternalDocumentMeta} - the documentation metadata containing relative link, storage
   *         directory, and language information; returns null if product not found or no documentation available
   * @author nqhoan
   */
  ExternalDocumentMeta findExternalDocument(String productId, String version);

  /**
   * <p>
   * Resolves a documentation path to the best matching URL redirect. Extracts product ID, artifact name,
   * and version from the provided path and generates a proper redirect URL by matching against available
   * symbolic links and documentation metadata. Handles development versions (dev, nightly) and regular
   * release versions by falling back to the best available match if exact version is not found.
   * </p>
   *
   * @param  path
   *              type {@link String} - the documentation path to resolve (format: /productId/version/doc or
   *              /productId/artifactName/version/doc); may include language code for localized documentation
   * @return {@link String} - the resolved redirect URL pointing to the actual documentation location;
   *         returns null if the path is invalid, product/artifact/version cannot be resolved, or no matching
   *         symbolic link exists
   * @author ttan
   */
  String resolveBestMatchRedirectUrl(String path);

  /**
   * <p>
   * Retrieves all available documentation versions and supported languages for a specific artifact.
   * Returns comprehensive documentation metadata including version-specific URLs and language-specific
   * alternatives. Sorts versions with development versions (dev) appearing last and release versions
   * in descending semantic order. Supports language fallback to English if the requested language
   * is not available for a specific version.
   * </p>
   *
   * @param artifact
   *              type {@link String} - the artifact name (e.g., "portal-guide") to find documentation for
   * @param version
   *              type {@link String} - the requested version for language variants; used to determine
   *              available language options for that specific version
   * @param language
   *              type {@link String} - the preferred language code (e.g., "en", "de"); if not available,
   *              documentation in other languages or English is returned as fallback
   * @param host
   *              type {@link String} - the base URL host to prepend to all relative documentation links
   * @return {@link DocumentInfoResponse} - contains sorted list of available versions with their URLs and
   *         list of supported languages for the requested version; returns null if no documentation metadata
   *         is found for the artifact
   * @author pvquan
   */
  DocumentInfoResponse findDocVersionsAndLanguages(String artifact, String version, String language, String host);

  /**
   * <p>
   * Determines which product IDs should be synchronized for external documentation. If a specific
   * product ID is provided, returns only that product. If no product ID is specified, returns all
   * product IDs that have documentation artifacts configured in the system.
   * </p>
   *
   * @param  productId
   *              type {@link String} - the specific product ID to synchronize (optional); if provided,
   *              only this product will be included; if blank or null, all products with documentation are included
   * @return {@link List<String>} - list of product IDs to be synchronized; returns single-element list
   *         if productId is specified, or all product IDs with documentation if productId is blank; returns
   *         empty list if no products with documentation exist and no specific productId is provided
   * @author ttan
   */
  List<String> determineProductIdsForSync(String productId);
}
